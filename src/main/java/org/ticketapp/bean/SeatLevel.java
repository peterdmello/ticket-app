package org.ticketapp.bean;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ticketapp.util.GeneratorUtil;

/**
 * Holds information of a particular Event seat level
 * @author peter
 *
 */
public class SeatLevel implements Comparable<SeatLevel>{
	private final int id;
	private final String name;
	private final Set<SeatRow> rows;
	private final Double price;
	public SeatLevel(int id, String name, Double price, int numRows, int numSeats) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.rows = new HashSet<>(numRows);
		for (int i = 1; i <= numRows; i++) {
			rows.add(new SeatRow(i, GeneratorUtil.getString(i), numSeats));
		}
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Set<SeatRow> getRows() {
		return rows;
	}
	public Double getPrice() {
		return price;
	}
	/**
	 * Returns the total seats in all rows
	 * @return total seats in all rows
	 */
	public int getTotalSeats() {
		return rows.parallelStream().mapToInt(row -> row.getTotalSeats()).sum();
	}
	/**
	 * Returns the number of available seats in all rows
	 * @return number of available seats in all rows
	 */
	public int getAvailableSeats() {
		return rows.parallelStream().mapToInt(row -> row.getAvailableSeats()).sum();
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
