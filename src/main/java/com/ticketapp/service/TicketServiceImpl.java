package com.ticketapp.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ticketapp.bean.Event;
import org.ticketapp.bean.SeatHold;

public class TicketServiceImpl implements TicketService {

	private final ConcurrentMap<Integer, Event> events;

	public TicketServiceImpl() {
		events = new ConcurrentHashMap<>();
	}

	@Override
	public int numSeatsAvailable(Optional<Integer> venueLevel) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel,
			String customerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		// TODO Auto-generated method stub
		return null;
	}

}
