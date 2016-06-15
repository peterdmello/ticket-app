package org.ticketapp.bean;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SeatIdentifier implements Comparable<SeatIdentifier> {
	private final int level;
	private final int row;
	private final int seat;

	public SeatIdentifier(int level, int row, int seat) {
		this.level = level;
		this.row = row;
		this.seat = seat;
	}

	public int getLevel() {
		return level;
	}

	public int getRow() {
		return row;
	}

	public int getSeat() {
		return seat;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(level).append(row).append(seat).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		SeatIdentifier otherSeatId = (SeatIdentifier) obj;
		return new EqualsBuilder().append(level, otherSeatId.level).append(row, otherSeatId.row)
				.append(seat, otherSeatId.seat).isEquals();
	};

	@Override
	public int compareTo(SeatIdentifier o) {
		return new CompareToBuilder().append(level, o.level).append(row, o.row).append(seat, o.seat).toComparison();
	}
}
