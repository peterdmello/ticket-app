package com.ticketapp.bean;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * An object of this class uniquely identifies a seat for an Event. Also, the
 * comparator of this class is responsible for defining which seat is the best
 * and which seat is hmmm... not so best (worst).
 * <p>
 * <b>Best Seat Calculation:</b>
 * <p>
 * As of now,
 * <ol>
 * <li>Lower the level, the better the seat.</li>
 * <li>Lower the row in a particular level, better the seat.</li>
 * <li>Lower the seat number, better the seat (this is not true in real life as
 * the center seats tend to be better, but this has been left out for
 * simplicity)</li>
 * </ol>
 * 
 * @author peter
 *
 */
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

	@Override
	public String toString() {
		return new StringBuilder("SeatIdentifier[level:").append(level).append(", row:").append(row).append(", seat:")
				.append(seat).append("]").toString();
	}
}
