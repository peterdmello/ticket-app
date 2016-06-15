package org.ticketapp.bean;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import org.ticketapp.bean.Seat.SeatState;

/**
 * Contains event state data.
 * 
 * @author peter
 *
 */
public class Event implements Comparable<Event> {

	private final int id;
	private final String name;
	private final ZonedDateTime startDateTime;
	private final long duration;
	private final ConcurrentMap<SeatIdentifier, Seat> seats;
	// private final Set<SeatLevel> levels;
	/**
	 * Seconds after which a hold on tickets will expire
	 */
	private long holdExpirationSeconds;

	public Event(int id, String name, ZonedDateTime startDT, long duration, List<SeatLevel> levels) {
		this.id = id;
		this.name = name;
		this.startDateTime = startDT;
		this.duration = duration;
		this.seats = new ConcurrentHashMap<>();

		for (SeatLevel level : levels) {
			for (int rowId = 1; rowId <= level.getRows(); rowId++) {
				for (int seatId = 1; seatId <= level.getSeats(); seatId++) {
					SeatIdentifier sid = new SeatIdentifier(level.getId(), rowId, seatId);
					seats.putIfAbsent(sid, new Seat(sid));
				}
			}
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ZonedDateTime getStartDateTime() {
		return startDateTime;
	}

	public long getDuration() {
		return duration;
	}

	/*
	 * public Set<SeatLevel> getLevels() { return
	 * Collections.unmodifiableSet(levels); }
	 */

	public long getHoldExpirationSeconds() {
		return holdExpirationSeconds;
	}

	public Map<SeatIdentifier, Seat> getSeats() {
		return Collections.unmodifiableMap(seats);
	}

	public int getAvailableSeats(Optional<Integer> levelId) {
		return getSeats(levelId, seat -> seat.getState() == SeatState.AVAILABLE);
	}

	public int getTotalSeats(Optional<Integer> levelId) {
		return getSeats(levelId, seat -> true);
	}

	private Integer getSeats(Optional<Integer> levelId, Predicate<Seat> predicate) {
		// get seats based on valid level id or for all levels
		
		return levelId.map(level -> seats.entrySet().parallelStream().filter(
				seatEntry -> seatEntry.getKey().getLevel() == level && predicate.test(seatEntry.getValue())).mapToInt(e -> 1).sum()
				).orElse(seats.entrySet().parallelStream().filter(seatEntry -> predicate.test(seatEntry.getValue())).mapToInt(e -> 1).sum());
		/*return levelId.map(
				level -> seats.entrySet().parallelStream().filter(
						seatEntry -> seatEntry.getKey().getLevel() == level && predicate.test(seatEntry.getValue())
					).mapToInt(e -> 1).sum());*/
		//return levelId.flatMap(level -> getLevel(level)).map(level -> mapper.apply(level)).orElse(levels.parallelStream().mapToInt(level -> mapper.apply(level)).sum());
	}

	/*private Optional<SeatLevel> getLevel(Integer levelId) {
		Optional<SeatLevel> providedLevel = levels.parallelStream().filter(level -> level.getId() == levelId)
				.findFirst();
		return providedLevel;
	}*/

	@Override
	public int compareTo(Event o) {
		return Integer.compare(id, o.id);
	}
}
