package com.ticketapp.bean;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Holds information of a particular Event seat level
 * @author peter
 *
 */
public class SeatLevel implements Comparable<SeatLevel>{
	private final int id;
	private final String name;
	private final int rows;
	private final int seats;
	private final Double price;
	public SeatLevel(int id, String name, Double price, int numRows, int numSeats) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.rows = numRows;
		this.seats = numSeats;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Double getPrice() {
		return price;
	}
	public int getRows() {
		return rows;
	}
	public int getSeats() {
		return seats;
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
		SeatLevel objSeatLevel = (SeatLevel) obj;
		return id == objSeatLevel.id;
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}
	@Override
	public int compareTo(SeatLevel o) {
		return Integer.compare(id, o.id);
	}
}
