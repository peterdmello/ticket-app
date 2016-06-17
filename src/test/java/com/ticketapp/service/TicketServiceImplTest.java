package com.ticketapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ticketapp.bean.Event;
import org.ticketapp.bean.Seat;
import org.ticketapp.bean.Seat.SeatState;
import org.ticketapp.bean.SeatHold;
import org.ticketapp.bean.SeatIdentifier;
import org.ticketapp.bean.input.EventInput;
import org.ticketapp.bean.input.LevelInput;

import com.ticketapp.service.TicketServiceImpl.ScheduledHold;
import com.ticketapp.service.exception.NotFoundException;
import com.ticketapp.service.exception.SeatHoldException;
import com.ticketapp.service.exception.SeatReservationException;

public class TicketServiceImplTest {

	private TicketServiceImpl ticketServiceImpl;
	private ScheduledExecutorService mockExecutor;
	private ConcurrentHashMap<Integer, ScheduledHold> holdCollection = new ConcurrentHashMap<>();
	@Rule public JUnitRuleMockery context = new JUnitRuleMockery();
	@Before
	public void before() {
		mockExecutor = context.mock(ScheduledExecutorService.class);
		context.checking(new Expectations() {{
			oneOf(mockExecutor).isShutdown();will(returnValue(false));
		}});
		ticketServiceImpl = new TicketServiceImpl(mockExecutor, new ConcurrentHashMap<>(), new ConcurrentHashMap<>(),
				holdCollection, new ConcurrentHashMap<>());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFirstEventShouldFailForNoEvents() {
		ticketServiceImpl.getFirstEvent();
	}

	@Test
	public void testGetFirstEventShouldReturnEvent() {
		String name = "hello";
		EventInput event = createEventInput(name);
		ticketServiceImpl.createEvent(event);
		Event firstEvent = ticketServiceImpl.getFirstEvent();
		assertNotNull(firstEvent);
		assertEquals(name, firstEvent.getName());
	}

	private EventInput createEventInput(String name) {
		return new EventInput(name, ZonedDateTime.now(), 1l,
				Collections.singletonList(new LevelInput("l1", 2.0, 1, 1)), 10);
	}

	private EventInput createEventInput(String name, List<LevelInput> lis) {
		return new EventInput(name, ZonedDateTime.now(), 1l,
				Collections.unmodifiableList(lis), 10);
	}
	@Test
	public void testShouldCreateMultipleEvents() {
		String name = "hello";
		EventInput event = createEventInput(name);
		Event createdEvent = ticketServiceImpl.createEvent(event);
		assertEquals(1, createdEvent.getId());
		assertEquals(name, createdEvent.getName());
		assertEquals(1, createdEvent.getAvailableSeatCount(Optional.empty()));
		SeatIdentifier seatId = createdEvent.getSeats().keySet().stream().findFirst().get();
		assertEquals(1, seatId.getLevel());
		assertEquals(1, seatId.getRow());
		assertEquals(1, seatId.getSeat());

		name = "bye";
		List<LevelInput> lis = new ArrayList<>();
		lis.add(new LevelInput("l1", 2.0, 1, 1));
		lis.add(new LevelInput("l2", 3.0, 1, 1));
		event = new EventInput(name, ZonedDateTime.now(), 1l, Collections.unmodifiableList(lis), 10);
		createdEvent = ticketServiceImpl.createEvent(event);
		assertEquals(2, createdEvent.getId());
		assertEquals(name, createdEvent.getName());
		assertEquals(2, createdEvent.getAvailableSeatCount(Optional.empty()));
		Seat seat = createdEvent.getSeats().get(new SeatIdentifier(1, 1, 1));
		assertEquals(1, seat.getId().getLevel());
		assertEquals(1, seat.getId().getRow());
		assertEquals(1, seat.getId().getSeat());
		seat = createdEvent.getSeats().get(new SeatIdentifier(2, 1, 1));
		assertEquals(2, seat.getId().getLevel());
		assertEquals(1, seat.getId().getRow());
		assertEquals(1, seat.getId().getSeat());
	}

	@Test(expected = SeatHoldException.class)
	public void testFindAvailableSeatsShouldThrowException() throws SeatHoldException {
		String name = "hello";
		List<LevelInput> lis = new ArrayList<>();
		lis.add(new LevelInput("l1", 2.0, 1, 1));
		lis.add(new LevelInput("l2", 3.0, 1, 1));
		EventInput event = new EventInput(name, ZonedDateTime.now(), 1l, Collections.unmodifiableList(lis), 10);
		ticketServiceImpl.createEvent(event);
		ticketServiceImpl.findSeats(1, 3, Optional.empty(), Optional.empty(), "a@b.com");
	}
	@Test(expected = IllegalArgumentException.class)
	public void testShouldThrowExceptionForBadSeatNum() throws SeatHoldException {
		String name = "hello";
		List<LevelInput> lis = new ArrayList<>();
		lis.add(new LevelInput("l1", 2.0, 1, 1));
		lis.add(new LevelInput("l2", 3.0, 1, 1));
		EventInput event = new EventInput(name, ZonedDateTime.now(), 1l, Collections.unmodifiableList(lis), 10);
		ticketServiceImpl.createEvent(event);
		ticketServiceImpl.findSeats(1, 0, Optional.empty(), Optional.empty(), "a@b.com");
	}
	@Test
	public void testShouldFindAvailableSeats() throws SeatHoldException {
		String name = "hello";
		List<LevelInput> lis = new ArrayList<>();
		lis.add(new LevelInput("l1", 2.0, 1, 1));
		lis.add(new LevelInput("l2", 3.0, 1, 1));
		EventInput event = new EventInput(name, ZonedDateTime.now(), 1l, Collections.unmodifiableList(lis), 10);
		ticketServiceImpl.createEvent(event);
		List<Seat> seats = ticketServiceImpl.findSeats(1, 2, Optional.empty(), Optional.empty(), "a@b.com");
		assertEquals(2, seats.size());
		assertEquals(new SeatIdentifier(1, 1, 1), seats.get(0).getId());
		assertEquals(new SeatIdentifier(2, 1, 1), seats.get(1).getId());
	}

	@Test
	public void testShouldHoldSeat() {
		String name = "hello";
		List<LevelInput> lis = new ArrayList<>();
		lis.add(new LevelInput("l1", 2.0, 1, 2));
		lis.add(new LevelInput("l2", 3.0, 1, 2));
		EventInput event = new EventInput(name, ZonedDateTime.now(), 1l, Collections.unmodifiableList(lis), 10);
		Event createdEvent = ticketServiceImpl.createEvent(event);
		List<Seat> availableSeats = Arrays.asList(new Seat(new SeatIdentifier(1, 1, 1), SeatState.AVAILABLE), new Seat(new SeatIdentifier(2, 1, 2), SeatState.AVAILABLE));
		ScheduledFuture<?> mockFuture = context.mock(ScheduledFuture.class);
		context.checking(new Expectations() {{
			oneOf(mockExecutor).schedule(with(any(Runnable.class)), with(equal(event.getHoldExpirationSeconds())), with(equal(TimeUnit.SECONDS)));will(returnValue(mockFuture));
		}});
		SeatHold seatHold = ticketServiceImpl.holdSeats(createdEvent, "a@b.com", availableSeats);
		assertEquals(1, seatHold.getId());
		assertEquals(availableSeats.stream().map(seat -> seat.getId()).collect(Collectors.toList()), seatHold.getSeatIds());
		context.assertIsSatisfied();
	}

	@Test(expected = SeatReservationException.class)
	public void testReserveSeatsShouldThrowException() {
		ticketServiceImpl.reserveSeats(5, "a@b.com");
	}

	@Test(expected = NotFoundException.class)
	public void testShouldThrowNotFoundExceptionForIncorrectCustomer() {
		holdCollection.put(1, new ScheduledHold(1, 1, new SeatHold(1, 1, "a@b.com", null), null));
		ticketServiceImpl.reserveSeats(1, "b@c.com");
	}
}
