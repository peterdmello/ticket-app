package org.ticketapp.bean;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Seat information and state
 * @author peter
 *
 */
public class Seat implements Comparable<Seat> {
	public enum SeatState {
		AVAILABLE, ON_HOLD, BOOKED
	}

	private final int id;
	private final SeatState state;
	private Integer holdId;
	private Integer reservationId;

	public Seat(int id) {
		this.id = id;
		this.state = SeatState.AVAILABLE;
	}

	public Seat(int id, SeatState state) {
		this.id = id;
		this.state = state;
	}

	public int getId() {
		return id;
	}

	public SeatState getState() {
		return state;
	}

	public Integer getHoldId() {
		return holdId;
	}

	public Integer getReservationId() {
		return reservationId;
	}

	public boolean isAvailable() {
		return state == SeatState.AVAILABLE;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		Seat objSeat = (Seat) obj;
		return id == objSeat.id;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

	@Override
	public int compareTo(Seat o) {
		return Integer.compare(id, o.id);
	}
}
