package org.ticketapp.bean;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.ticketapp.bean.input.LevelInput;

public class EventTest {

	@Test
	public void testConstructor() {
		List<LevelInput> levels = new ArrayList<>();
		LevelInput li = new LevelInput("Orchestra", 100.0, 25, 50);
		levels.add(li);
		li = new LevelInput("Main", 75.0, 20, 100);
		levels.add(li);
		li = new LevelInput("Balcony 1", 50.0, 15, 100);
		levels.add(li);
		li = new LevelInput("Balcony 2", 40.0, 15, 100);
		levels.add(li);
		String eventName = "Awesome concert";
		int eventDuration = 120;
		Event event = new Event(1, eventName, ZonedDateTime.now(), eventDuration, levels);
		assertEquals(eventName, event.getName());
		assertEquals(eventDuration, event.getDuration());
		assertEquals(6250, event.getAvailableSeats(Optional.empty()));
		assertEquals(1250, event.getAvailableSeats(Optional.of(1)));
		assertEquals(2000, event.getAvailableSeats(Optional.of(2)));
		assertEquals(1500, event.getAvailableSeats(Optional.of(3)));
		assertEquals(1500, event.getAvailableSeats(Optional.of(4)));
		assertEquals(6250, event.getAvailableSeats(Optional.of(5)));
		assertEquals(6250, event.getTotalSeats(Optional.empty()));
		assertEquals(1250, event.getTotalSeats(Optional.of(1)));
		assertEquals(2000, event.getTotalSeats(Optional.of(2)));
		assertEquals(1500, event.getTotalSeats(Optional.of(3)));
		assertEquals(1500, event.getTotalSeats(Optional.of(4)));
		assertEquals(6250, event.getTotalSeats(Optional.of(5)));
	}

}
