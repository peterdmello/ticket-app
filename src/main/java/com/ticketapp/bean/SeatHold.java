package com.ticketapp.bean;

import java.util.List;

/**
 * Object indicating a hold on seats by a user
 * @author peter
 *
 */
public class SeatHold {
	private final String emailId;
	private final List<SeatIdentifier> seatIds;
	private final int id;
	private final int eventId;

	public SeatHold(int eventId, int holdId, String emailId, List<SeatIdentifier> seatIds) {
		this.eventId = eventId;
		this.emailId = emailId;
		this.seatIds = seatIds;
		this.id = holdId;
	}

	public String getEmailId() {
		return emailId;
	}

	public List<SeatIdentifier> getSeatIds() {
		return seatIds;
	}

	public int getId() {
		return id;
	}

	public int getEventId() {
		return eventId;
	}

	@Override
	public String toString() {
		return new StringBuilder("SeatHold[id: ").append(id).append(", eventId: ").append(eventId).append(", seats: ")
				.append(seatIds).append("]").toString();
	}
}
