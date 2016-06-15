package org.ticketapp.bean.input;

import java.time.ZonedDateTime;
import java.util.List;

public class EventInput {
	private final String name;
	private final ZonedDateTime startDateTime;
	private final long duration;
	private final List<LevelInput> levels;
	public EventInput(String name, ZonedDateTime startDT, long duration, List<LevelInput> levels) {
		this.name = name;
		this.startDateTime = startDT;
		this.duration = duration;
		this.levels = levels;
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
	public List<LevelInput> getLevels() {
		return levels;
	}
	
}
