package com.ticketapp.bean.input;

import java.time.ZonedDateTime;
import java.util.List;

public class EventInput {
	private final String name;
	private final ZonedDateTime startDateTime;
	private final long duration;
	private final List<LevelInput> levels;
	private final long holdExpirationSeconds;
	public EventInput(String name, ZonedDateTime startDT, long duration, List<LevelInput> levels, long holdExpirationSeconds) {
		this.name = name;
		this.startDateTime = startDT;
		this.duration = duration;
		this.levels = levels;
		this.holdExpirationSeconds = holdExpirationSeconds;
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
	public long getHoldExpirationSeconds() {
		return holdExpirationSeconds;
	}
}
