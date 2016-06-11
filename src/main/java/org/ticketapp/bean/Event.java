package org.ticketapp.bean;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.ticketapp.bean.input.LevelInput;

/**
 * Contains event related data.
 * 
 * @author peter
 *
 */
public class Event implements Comparable<Event> {

	private final int id;
	private final String name;
	private final ZonedDateTime startDateTime;
	private final long duration;
	private final Set<SeatLevel> levels;
	/**
	 * Seconds after which a hold on tickets will expire
	 */
	private long holdExpirationSeconds;

	public Event(int id, String name, ZonedDateTime startDT, long duration, List<LevelInput> levelInfos) {
		this.id = id;
		this.name = name;
		this.startDateTime = startDT;
		this.duration = duration;
		this.levels = new HashSet<>(levelInfos.size());
		int levelNum = 1;
		for (LevelInput levelInfo : levelInfos) {
			this.levels.add(new SeatLevel(levelNum++, levelInfo.getName(), levelInfo.getPrice(), levelInfo.getRows(),
					levelInfo.getSeatsInRow()));
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

	public Set<SeatLevel> getLevels() {
		return Collections.unmodifiableSet(levels);
	}

	public long getHoldExpirationSeconds() {
		return holdExpirationSeconds;
	}

	public int getAvailableSeats(Optional<Integer> levelId) {
		return getSeats(levelId, level -> level.getAvailableSeats());
	}

	public int getTotalSeats(Optional<Integer> levelId) {
		return getSeats(levelId, level -> level.getTotalSeats());
	}

	private Integer getSeats(Optional<Integer> levelId, Function<SeatLevel, Integer> mapper) {
		return levelId.flatMap(level -> getLevel(level)).map(level -> mapper.apply(level)).orElse(levels.parallelStream().mapToInt(level -> mapper.apply(level)).sum());
		/*if (levelId != null && levelId.isPresent()) {
			Optional<SeatLevel> providedLevel = getLevel(levelId.get());
			if (providedLevel.isPresent()) {
				return mapper.apply(providedLevel.get());
			} else {
				return -1;
			}
		} else {
			return levels.parallelStream().mapToInt(level -> mapper.apply(level)).sum();
		}*/
	}

	private Optional<SeatLevel> getLevel(Integer levelId) {
		Optional<SeatLevel> providedLevel = levels.parallelStream().filter(level -> level.getId() == levelId)
				.findFirst();
		return providedLevel;
	}

	@Override
	public int compareTo(Event o) {
		return Integer.compare(id, o.id);
	}
}
