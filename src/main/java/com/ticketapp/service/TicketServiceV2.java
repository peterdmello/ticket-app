package com.ticketapp.service;

import com.ticketapp.bean.Event;
import com.ticketapp.bean.input.EventInput;

/**
 * Interface with additional ticket service methods
 * @author peter
 * @see {@link TicketService}
 */
public interface TicketServiceV2 {
	Event createEvent(EventInput eventInput);
	Event getEvent(Integer id);
}
