package com.ticketapp.service;

import org.ticketapp.bean.Event;
import org.ticketapp.bean.input.EventInput;

public interface TicketServiceV2 {

	Event createEvent(EventInput eventInput);
	Event getEvent(Integer id);
}
