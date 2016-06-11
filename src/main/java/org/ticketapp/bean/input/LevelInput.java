package org.ticketapp.bean.input;

public class LevelInput {
	private final String name;
	private final Double price;
	private final int rows;
	private final int seatsInRow;
	public LevelInput(String name, Double price, int rows, int seats) {
		this.name = name;
		this.price = price;
		this.rows = rows;
		this.seatsInRow = seats;
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
	public int getSeatsInRow() {
		return seatsInRow;
	}
}
