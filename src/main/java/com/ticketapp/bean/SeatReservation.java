package com.ticketapp.bean;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Event reservation object
 * @author peter
 *
 */
public final class SeatReservation {
	private final UUID id;
	private final int eventId;
	private final List<SeatIdentifier> seatIds;

	public SeatReservation(UUID id, int eventId, List<SeatIdentifier> seatIds) {
		this.id = id;
		this.eventId = eventId;
		this.seatIds = seatIds;
	}

	public int getEventId() {
		return eventId;
	}

	public UUID getId() {
		return id;
	}

	public List<SeatIdentifier> getSeatIds() {
		return Collections.unmodifiableList(seatIds);
	}

	@Override
	public String toString() {
		return new StringBuilder("SeatReservation[id: ").append(id).append(", eventId: ").append(eventId)
				.append(", seatIds: ").append(seatIds).append("]").toString();
	}
}
