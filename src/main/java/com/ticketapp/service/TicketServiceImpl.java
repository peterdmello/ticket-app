package com.ticketapp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ticketapp.bean.Event;
import org.ticketapp.bean.Seat;
import org.ticketapp.bean.Seat.SeatState;
import org.ticketapp.bean.SeatHold;
import org.ticketapp.bean.SeatIdentifier;
import org.ticketapp.bean.SeatLevel;
import org.ticketapp.bean.input.EventInput;

import com.ticketapp.service.exception.SeatHoldException;

/**
 * Implementation of {@link TicketService} which provides concurrent read/writes
 * to <b>an</b> {@link Event} object. Though provision is provided to have
 * multiple {@link Event}s, the service only works on the first fetched
 * {@link Event} to fulfill the interface's requirement.
 * 
 * <p>
 * <b>Concurrency Implementation:</b> Each {@link Event} has a unique lock
 * associated with it. Reads are multi-threaded while writes are upgraded from
 * read-to-write before updating the {@link Event} object as required. Why
 * {@link ConcurrentHashMap} (CHM) was not used:
 * <ol>
 * <li>CHM is best suited when used as a cache and when writes are few compared
 * to reads.</li>
 * <li>CHM can give a state of the map while being written. This may lead to a
 * possibility of returning previous values and a possibility of overwriting
 * values
 * <li>The previous issue of overwriting can be overcome by using
 * {@link ConcurrentHashMap#replace(Object, Object, Object), however this makes
 * the code (entire transaction management for entire held seats complicated)
 * and practically time consuming, unpredictable and irritating as for each
 * failed replace, the user will have to select a new seat.</ol>
 * 
 * <b>Concurrency Improvements:</b> If operations which acquire a write lock
 * take too long, one of the way to improve concurrency is to use finer
 * granularity of locking. We can use locks on the SeatLevel. This will add more
 * complexity, especially when a single hold is on multiple seat levels
 * (multiple write locks need to be acquired), but, the chances of that
 * occurring should be less frequent for a hold (depending on usecase).
 * 
 * @author peter
 *
 */
public final class TicketServiceImpl implements TicketService, TicketServiceV2 {

	/**
	 * Map to hold eventId and corresponding events
	 */
	private final ConcurrentMap<Integer, EventSync> events;
	/**
	 * Id generators
	 */
	private final AtomicInteger eventIdGen, holdIdGen;
	/**
	 * Map of event to {@link SeatLevel}s List. Assumption: seat levels are
	 * ordered from best to worst
	 */
	private final ConcurrentMap<Integer, List<SeatLevel>> seatLevels;
	private final ScheduledExecutorService holdScheduler;
	/**
	 * Map of holdId to object ScheduledHold object to manage hold
	 */
	private final ConcurrentMap<Integer, ScheduledHold> holdCollection;

	private static final Logger LOG = LoggerFactory.getLogger(TicketServiceImpl.class);

	/**
	 * Instantiate as follows: <code>
	 * TicketService ticketService = new TicketServiceImpl(Executors.newScheduledThreadPool(4), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
	 * </code>
	 * 
	 * @param holdScheduler
	 *            An instance of ScheduledExecutorService to handle expired seat
	 *            holds
	 * @param events
	 *            map holding eventid and corresponding events with their locks
	 * @param seatLevels
	 *            map holding eventid and corresponding levels
	 * @param holdCollection
	 *            map holding holdid and hold object containing ScheduledFuture
	 *            to control the hold
	 */
	public TicketServiceImpl(ScheduledExecutorService holdScheduler, ConcurrentMap<Integer, EventSync> events, ConcurrentMap<Integer, List<SeatLevel>> seatLevels, ConcurrentMap<Integer, ScheduledHold> holdCollection) {
		if (holdScheduler == null || holdScheduler.isShutdown()) {
			throw new IllegalArgumentException("Invalid hold scheduler");
		}
		this.eventIdGen = new AtomicInteger(1);
		this.holdIdGen = new AtomicInteger(1);
		this.events = events;
		this.seatLevels = seatLevels;
		this.holdCollection = holdCollection;
		this.holdScheduler = holdScheduler;
	}

	@Override
	public Event createEvent(EventInput eventInput) {
		int eventId = eventIdGen.getAndIncrement();
		List<SeatLevel> seatLevels = new ArrayList<>();
		AtomicInteger seatLevel = new AtomicInteger(1);
		seatLevels
				.addAll(eventInput
						.getLevels().stream().map(level -> new SeatLevel(seatLevel.getAndIncrement(), level.getName(),
								level.getPrice(), level.getRows(), level.getSeatsInRow()))
				.collect(Collectors.toList()));
		Event event = new Event(eventId, eventInput.getName(), eventInput.getStartDateTime(), eventInput.getDuration(),
				seatLevels, eventInput.getHoldExpirationSeconds());
		events.put(eventId, new EventSync(event, new ReentrantReadWriteLock()));
		this.seatLevels.put(eventId, Collections.unmodifiableList(seatLevels));
		LOG.info("Event created: {}", event);
		return event;
	}

	@Override
	public Event getEvent(Integer id) {
		// TODO: read lock
		return events.get(id).getEvent();
	}

	@Override
	public int numSeatsAvailable(Optional<Integer> venueLevel) {
		// TODO: read lock
		Event firstEvent = getFirstEvent();
		return firstEvent.getAvailableSeatCount(venueLevel);
	}

	/**
	 * This method returns the first {@link Event} in the collection. Ideally,
	 * this method should not exist and we should be getting all events by id.
	 * 
	 * @return first event object found in list
	 * @throws IllegalStateException
	 *             if no {@link Event} is present in collection
	 */
	Event getFirstEvent() {
		// TODO: read lock
		return events.values().stream().findFirst().map(obj -> obj.getEvent())
				.orElseThrow(() -> new IllegalStateException("no events present"));
	}

	List<Seat> findSeats(int eventId, int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel, String customerEmail) {
		// check total seats
		if (numSeats < 1) {
			throw new IllegalArgumentException("Invalid seats requested: " + numSeats);
		}
		Event event = getEvent(eventId);
		List<Seat> seats =  event.getBestAvailableSeats(minLevel, maxLevel, numSeats);
		if (seats.size() < numSeats) {
			throw new SeatHoldException(String.format("{} seats not available between levels {} and {}", numSeats, minLevel, maxLevel));
		}
		return seats;
	}

	/**
	 * Add a hold on the seats. The hold can either proceed to a booking or expire in which case seats will be reclaimed
	 * @param event
	 * @param emailId
	 * @param availableSeat
	 * @return
	 */
	SeatHold holdSeats(Event event, String emailId, List<Seat> availableSeat) {
		// create new seats and update seats map // need to update Event and Seat constructor
		SeatHold seatHold = new SeatHold(event.getId(), holdIdGen.getAndIncrement(), emailId, availableSeat.stream().map(seat -> seat.getId()).collect(Collectors.toList()));
		// schedule task to reclaim held seats if booking doesn't occur in timely manner
		ScheduledFuture<?> future = this.holdScheduler.schedule(() -> reclaimHold(event.getId(), seatHold.getId()), event.getHoldExpirationSeconds(), TimeUnit.SECONDS);
		this.holdCollection.putIfAbsent(seatHold.getId(), new ScheduledHold(event.getId(), seatHold.getId(), seatHold, future));
		LOG.info("Event hold created: {}. Expires in {}s", seatHold, event.getHoldExpirationSeconds());
		return seatHold;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel,
			String customerEmail) {
		// TODO: read lock
		/*
		 * acquire read lock
		 * check if seats available - List<Seat>
		 * if not throw exception SeatHoldException
		 * if available, unlock readlock, upgrade to write lock, check again
		 *  i. get seats and set state to hold
		 *  ii. update event object (essentially create new Event object reusing as much as possible)...
		 *  iii. call holdSeat which will 
		 * holdSeat:
		 *  i. create SeatHold object and store it
		 *  ii. add to ScheduledExecutorService to reclaim seats if time expires
		 */
		Event event = getFirstEvent();
		List<Seat> availableSeats = findSeats(event.getId(), numSeats, minLevel, maxLevel, customerEmail);
		// update seats to HOLD
		// update event object (update map)
		Event updatedEvent = updateEventSeats(event.getId(),
				Collections.singletonMap(SeatState.ON_HOLD, availableSeats));
		// update event map
		EventSync oldEventSync = events.get(event.getId());
		events.put(updatedEvent.getId(), new EventSync(updatedEvent, oldEventSync.getLock()));
		// create scheduled hold
		return holdSeats(updatedEvent, customerEmail, availableSeats);
	}

	/**
	 * Called by scheduled executor when time expires
	 * @param eventId
	 * @param holdId
	 */
	public void reclaimHold(int eventId, int holdId) {
		// TODO: write lock
		ScheduledHold scheduledHold = this.holdCollection.get(holdId);
		// get seats
		List<SeatIdentifier> reclaimSeatIds = scheduledHold.getHold().getSeatIds();
		// set seats to AVAILABLE
		List<Seat> reclaimSeats = reclaimSeatIds.stream().map(seatId -> new Seat(seatId, SeatState.ON_HOLD)).collect(Collectors.toList());
		// update event
		Event updatedEvent = this.updateEventSeats(eventId, Collections.singletonMap(SeatState.AVAILABLE, reclaimSeats));
		// remove from scheduledMap
		EventSync oldEventSync = events.get(eventId);
		events.put(updatedEvent.getId(), new EventSync(updatedEvent, oldEventSync.getLock()));
		this.holdCollection.remove(holdId);
		LOG.info("Reclaimed seats {} from holdId {} for eventId {}", reclaimSeatIds, holdId, eventId);
	}

	Event updateEventSeats(int eventId, Map<SeatState, List<Seat>> updatedSeats) {
		// TODO: write lock
		// update map
		Event event = getEvent(eventId);
		return new Event(event, updatedSeats);
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		// TODO: write lock
		// check if holdId exists in collection, if not throw exception
		// cancel scheduled task
		// updateEvent
		// remove from hold
		// add to reserved list
		return null;
	}

	static final class EventSync {
		private final Event event;
		private final ReentrantReadWriteLock lock;

		public EventSync(Event event, ReentrantReadWriteLock lock) {
			this.event = event;
			this.lock = lock;
		}

		public Event getEvent() {
			return event;
		}

		public ReentrantReadWriteLock getLock() {
			return lock;
		}
	}
	static final class ScheduledHold {
		private final Integer eventId;
		private final Integer holdId;
		private final SeatHold hold;
		private final ScheduledFuture<?> scheduledFuture;
		public ScheduledHold(Integer eventId, Integer holdId, SeatHold hold, ScheduledFuture<?> scheduledFuture) {
			this.eventId = eventId;
			this.holdId = holdId;
			this.hold = hold;
			this.scheduledFuture = scheduledFuture;
		}
		public Integer getEventId() {
			return eventId;
		}
		public SeatHold getHold() {
			return hold;
		}
		public Integer getHoldId() {
			return holdId;
		}
		public ScheduledFuture<?> getScheduledFuture() {
			return scheduledFuture;
		}
	}
}
