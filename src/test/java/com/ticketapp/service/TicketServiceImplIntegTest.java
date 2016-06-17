package com.ticketapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ticketapp.bean.Event;
import org.ticketapp.bean.SeatHold;
import org.ticketapp.bean.SeatReservation;
import org.ticketapp.bean.input.EventInput;
import org.ticketapp.bean.input.LevelInput;

import com.ticketapp.service.TicketServiceImpl.ScheduledHold;
import com.ticketapp.service.exception.SeatHoldException;
import com.ticketapp.service.exception.SeatReservationException;

public class TicketServiceImplIntegTest {

	private TicketService ticketService;
	private TicketServiceV2 ticketServiceV2;
	private int eventId;
	private ScheduledExecutorService executorService;
	private static final long TIMEOUT = 30000;
	private ConcurrentHashMap<Integer, ScheduledHold> holdCollection = new ConcurrentHashMap<>();
	private ConcurrentHashMap<UUID, SeatReservation> reservations = new ConcurrentHashMap<>();
	@Before
	public void before() {
		executorService = Executors.newScheduledThreadPool(4);
		TicketServiceImpl ticketServiceImpl = new TicketServiceImpl(Executors.newScheduledThreadPool(4), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), holdCollection, reservations);
		ticketService = ticketServiceImpl;
		ticketServiceV2 = ticketServiceImpl;
		EventInput ei = new EventInput("test", ZonedDateTime.now(), 5, Arrays.asList(new LevelInput("l1", 5.0, 2, 2)), 10);
		Event event = ticketServiceV2.createEvent(ei);
		eventId = event.getId();
	}

	@After
	public void after() throws InterruptedException {
		executorService.shutdown();
		executorService.awaitTermination(20, TimeUnit.SECONDS);
	}

	@Test(timeout = TIMEOUT)
	public void testSeatsShouldBeReclaimedAfterTimeout() throws InterruptedException {
		SeatHold hold = ticketService.findAndHoldSeats(2, Optional.empty(), Optional.empty(), "a@b.com");
		assertEquals(1, hold.getId());
		ScheduledHold shold = holdCollection.get(hold.getId());
		assertNotNull(shold);
		assertSame(shold.getHold(), hold);
		executorService.awaitTermination(20, TimeUnit.SECONDS);
		executorService.shutdown();
		assertEquals(4, ticketService.numSeatsAvailable(Optional.empty()));
		assertTrue(holdCollection.isEmpty());
	}

	@Test(timeout = TIMEOUT)
	public void testShouldFindAvailableSeatsAfterHoldSeats() throws InterruptedException {
		SeatHold hold = ticketService.findAndHoldSeats(2, Optional.empty(), Optional.empty(), "a@b.com");
		assertEquals(2, ticketService.numSeatsAvailable(Optional.empty()));
	}

	@Test(timeout = TIMEOUT)
	public void testShouldThrowSeatHoldExceptionIfNotEnoughSeats() {
		ticketService.findAndHoldSeats(2, Optional.empty(), Optional.empty(), "a@b.com");
		try {
			ticketService.findAndHoldSeats(3, Optional.empty(), Optional.empty(), "b@c.com");
		} catch (SeatHoldException ex) {
			// do nothing
		}
	}

	@Test(timeout = TIMEOUT)
	public void testShouldReserveSeats() throws InterruptedException {
		SeatHold hold = ticketService.findAndHoldSeats(2, Optional.empty(), Optional.empty(), "a@b.com");
		ScheduledHold scheduledHold = holdCollection.get(hold.getId());
		String reservationCode = ticketService.reserveSeats(hold.getId(), "a@b.com");
		// hold should be gone and ScheduledFuture cancelled
		assertTrue(holdCollection.isEmpty());
		assertTrue(scheduledHold.getScheduledFuture().isCancelled());
		assertTrue(scheduledHold.getScheduledFuture().isDone());
		// check reservation
		SeatReservation reservation = reservations.get(UUID.fromString(reservationCode));
		assertNotNull(reservation);
		assertEquals(reservation.getSeatIds(), hold.getSeatIds());
		executorService.awaitTermination(20, TimeUnit.SECONDS);
		executorService.shutdown();
		// check persistence, ensure that seats are not reclaimed after hold timeout
		assertEquals(2, ticketService.numSeatsAvailable(Optional.empty()));
	}

	@Test(timeout = TIMEOUT, expected = SeatReservationException.class)
	public void testReserveSeatsShouldFailDueToHoldExpiration() throws InterruptedException {
		SeatHold hold = ticketService.findAndHoldSeats(2, Optional.empty(), Optional.empty(), "a@b.com");
		assertEquals(1, hold.getId());
		ScheduledHold shold = holdCollection.get(hold.getId());
		executorService.awaitTermination(20, TimeUnit.SECONDS);
		ticketService.reserveSeats(hold.getId(), "a@b.com");
	}
}
