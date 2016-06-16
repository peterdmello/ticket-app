package com.ticketapp.service;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ticketapp.bean.Event;
import org.ticketapp.bean.SeatHold;
import org.ticketapp.bean.input.EventInput;
import org.ticketapp.bean.input.LevelInput;

import com.ticketapp.service.TicketServiceImpl.ScheduledHold;

public class TicketServiceImplIntegTest {

	private TicketService ticketService;
	private TicketServiceV2 ticketServiceV2;
	private int eventId;
	private ScheduledExecutorService executorService;
	private static final long TIMEOUT = 30000;
	ConcurrentHashMap<Integer, ScheduledHold> holdCollection = new ConcurrentHashMap<>();
	@Before
	public void before() {
		executorService = Executors.newScheduledThreadPool(4);
		TicketServiceImpl ticketServiceImpl = new TicketServiceImpl(Executors.newScheduledThreadPool(4), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), holdCollection);
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
	public void testShouldFindAndHoldSeats() throws InterruptedException {
		SeatHold hold = ticketService.findAndHoldSeats(2, Optional.empty(), Optional.empty(), "a@b.com");
		assertEquals(1, hold.getId());
		executorService.awaitTermination(20, TimeUnit.SECONDS);
		executorService.shutdown();
		assertEquals(4, ticketService.numSeatsAvailable(Optional.empty()));
	}

	@Test(timeout = TIMEOUT)
	public void testShouldFindAvailableSeatsAfterHoldSeats() throws InterruptedException {
		SeatHold hold = ticketService.findAndHoldSeats(2, Optional.empty(), Optional.empty(), "a@b.com");
		assertEquals(2, ticketService.numSeatsAvailable(Optional.empty()));
	}
}
