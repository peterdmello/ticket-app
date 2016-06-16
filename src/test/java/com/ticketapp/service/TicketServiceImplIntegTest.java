package com.ticketapp.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.junit.Before;

public class TicketServiceImplIntegTest {

	private TicketService ticketService;
	private TicketServiceV2 ticketServiceV2;
	@Before
	public void before() {
		ticketService = new TicketServiceImpl(Executors.newScheduledThreadPool(4), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>()); 
	}
}
