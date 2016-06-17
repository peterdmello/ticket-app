package com.ticketapp.bean;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Seat information and state.
 * <p>
 * Currently, the state is not used to uniquely identify the object. On the
 * fence if this should be the case or not. From a caching point of view, it
 * would be a bad idea.
 * 
 * @author peter
 *
 */
public class Seat implements Comparable<Seat> {
	public enum SeatState {
		AVAILABLE, ON_HOLD, BOOKED
	}

	private final SeatIdentifier id;
	private final SeatState state;

	public Seat(SeatIdentifier id) {
		this.id = id;
		this.state = SeatState.AVAILABLE;
	}

	public Seat(SeatIdentifier id, SeatState state) {
		this.id = id;
		this.state = state;
	}

	public SeatIdentifier getId() {
		return id;
	}

	public SeatState getState() {
		return state;
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
		return id.compareTo(o.id);
	}
}
