package com.ticketapp.bean;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import com.ticketapp.bean.Seat.SeatState;

/**
 * Contains event state data. Object is immutable.
 * TODO: use Builder pattern instead of multiple ugly constructors
 * @author peter
 */
public class Event implements Comparable<Event> {

	private final int id;
	private final String name;
	private final ZonedDateTime startDateTime;
	private final long duration;
	private final ConcurrentMap<SeatIdentifier, Seat> seats;
	private final int bestLevel;
	private final int worstLevel;
	/**
	 * Ordered set which returns the current best available seats
	 */
	private final Set<Seat> bestSeats;
	/**
	 * Says when the object was created. Useful to determine validity of object
	 */
	private final long createdTime;

	/**
	 * Seconds after which a hold on tickets will expire
	 */
	private final long holdExpirationSeconds;

	public Event(int id, String name, ZonedDateTime startDT, long duration, List<SeatLevel> levels,
			long expirationSeconds) {
		this.id = id;
		this.name = name;
		this.startDateTime = startDT;
		this.duration = duration;
		this.holdExpirationSeconds = expirationSeconds;
		this.seats = new ConcurrentHashMap<>();
		this.bestSeats = new TreeSet<>();
		for (SeatLevel level : levels) {
			for (int rowId = 1; rowId <= level.getRows(); rowId++) {
				for (int seatId = 1; seatId <= level.getSeats(); seatId++) {
					SeatIdentifier sid = new SeatIdentifier(level.getId(), rowId, seatId);
					Seat seat = new Seat(sid);
					Seat oldSeat = seats.putIfAbsent(sid, seat);
					assert oldSeat == null; // testing code
					bestSeats.add(seat);
				}
			}
		}
		this.bestLevel = levels.get(0).getId();
		this.worstLevel = levels.get(levels.size() - 1).getId();
		this.createdTime = System.currentTimeMillis();
	}

	public Event(Event oldEvent, Map<SeatState, List<Seat>> seatStateUpdates) {
		// validate that information is current (i.e we are trying to update
		// from the correct state to the correct state)
		this.seats = new ConcurrentHashMap<>();
		this.bestSeats = new TreeSet<>();
		this.id = oldEvent.id;
		this.name = oldEvent.getName();
		this.startDateTime = oldEvent.startDateTime;
		this.duration = oldEvent.duration;
		this.holdExpirationSeconds = oldEvent.holdExpirationSeconds;

		int tmpBestLevel = Integer.MAX_VALUE;
		int tmpWorstLevel = Integer.MIN_VALUE;
		for (Entry<SeatState, List<Seat>> entry : seatStateUpdates.entrySet()) {
			SeatState newState = entry.getKey();
			for (Seat updateSeat : entry.getValue()) {
				Seat oldSeat = oldEvent.getSeats().get(updateSeat.getId());
				if (oldSeat.getState() != updateSeat.getState()) {
					throw new IllegalArgumentException(String.format("State update to {} failed: {} is not in state {}",
							newState, updateSeat, oldSeat.getState()));
				} else {
					Seat newSeat = new Seat(updateSeat.getId(), newState);
					Seat prevSeat = this.seats.putIfAbsent(updateSeat.getId(), newSeat);
					assert prevSeat == null;
					if (newState == SeatState.AVAILABLE) {
						this.bestSeats.add(newSeat);
					}
				}
			}
		}

		for (Entry<SeatIdentifier, Seat> oldSeatEntry : oldEvent.seats.entrySet()) {
			SeatIdentifier seatId = oldSeatEntry.getKey();
			Seat oldSeat = oldSeatEntry.getValue();
			Seat newSeat = this.seats.putIfAbsent(seatId, oldSeat);
			// if newSeat != null, it was already added, so, don't overwrite it
			if (newSeat == null && oldSeat.getState() == SeatState.AVAILABLE) {
				this.bestSeats.add(oldSeat);
			}
			if (seatId.getLevel() < tmpBestLevel) {
				tmpBestLevel = seatId.getLevel();
			}
			if (tmpWorstLevel < seatId.getLevel()) {
				tmpWorstLevel = seatId.getLevel();
			}
		}
		this.bestLevel = tmpBestLevel;
		this.worstLevel = tmpWorstLevel;
		createdTime = System.currentTimeMillis();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ZonedDateTime getStartDateTime() {
		return startDateTime;
	}

	public long getDuration() {
		return duration;
	}

	public long getHoldExpirationSeconds() {
		return holdExpirationSeconds;
	}

	public Map<SeatIdentifier, Seat> getSeats() {
		return Collections.unmodifiableMap(seats);
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public int getAvailableSeatCount(Optional<Integer> levelId) {
		return getSeatCount(levelId, seat -> seat.getState() == SeatState.AVAILABLE);
	}

	public int getTotalSeatCount(Optional<Integer> levelId) {
		return getSeatCount(levelId, seat -> true);
	}

	private Integer getSeatCount(Optional<Integer> levelId, Predicate<Seat> predicate) {
		// get seats based on valid level id or for all levels

		return levelId
				.map(level -> seats.entrySet().parallelStream()
						.filter(seatEntry -> seatEntry.getKey().getLevel() == level
								&& predicate.test(seatEntry.getValue()))
						.mapToInt(e -> 1).sum())
				.orElse(seats.entrySet().parallelStream().filter(seatEntry -> predicate.test(seatEntry.getValue()))
						.mapToInt(e -> 1).sum());
	}

	public List<Seat> getBestAvailableSeats(Optional<Integer> minLevel, Optional<Integer> maxLevel, int count) {
		/*
		 * since this is an immutable object, it's kinda alright to have
		 * business logic here. Moving it out to the service should not be
		 * difficult too if this is a concern.
		 */
		int startLevel = minLevel.map(level -> level).orElse(bestLevel);
		int endLevel = maxLevel.map(level -> level).orElse(worstLevel);
		List<Seat> bestList = new ArrayList<>();
		Iterator<Seat> seatIterator = bestSeats.stream()
				.filter(seat -> seat.getId().getLevel() >= startLevel && seat.getId().getLevel() <= endLevel)
				.iterator();
		while (bestList.size() < count && seatIterator.hasNext()) {
			bestList.add(seatIterator.next());
		}
		return Collections.unmodifiableList(bestList);
	}

	@Override
	public int compareTo(Event o) {
		return Integer.compare(id, o.id);
	}

	@Override
	public String toString() {
		return new StringBuilder("Event[id: ").append(id).append(", name: ").append(name).append(", totalSeats: ")
				.append(seats.size()).append(", available: ").append(getAvailableSeatCount(Optional.empty()))
				.append("]").toString();
	}
}
