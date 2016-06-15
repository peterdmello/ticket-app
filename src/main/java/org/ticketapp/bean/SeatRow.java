package org.ticketapp.bean;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Holds information about a particular row in the seat level of an event
 * @author peter
 *
 */
public class SeatRow implements Comparable<SeatRow>{
	private final int id;
	private final String name;
	//private final Set<Seat> seats;
	public SeatRow(int id, String name, int countSeats) {
		this.id = id;
		this.name = name;
		/*this.seats  = new HashSet<>(countSeats);
		for (int i = 1; i <= countSeats; i++) {
			seats.add(new Seat(i));
		}*/
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	/*public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(seats);
	}
	public int getTotalSeats() {
		return seats.size();
	}
	public int getAvailableSeats() {
		return (int) seats.parallelStream().filter(seat -> seat.getState() == Seat.SeatState.AVAILABLE).count();
	}*/
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
		SeatRow objSeatRow = (SeatRow) obj;
		return id == objSeatRow.id;
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}
	@Override
	public int compareTo(SeatRow o) {
		return Integer.compare(id, o.id);
	}
}
