package org.ticketapp.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.ticketapp.bean.Seat.SeatState;
import org.ticketapp.bean.input.EventInput;
import org.ticketapp.bean.input.LevelInput;

public class EventTest {

	private static final String eventName = "Awesome concert";
	private static final int eventDuration = 120;
	private Event event;

	@Before
	public void before() {
		List<LevelInput> levels = new ArrayList<>();
		LevelInput li = new LevelInput("Orchestra", 100.0, 25, 50);
		levels.add(li);
		li = new LevelInput("Main", 75.0, 20, 100);
		levels.add(li);
		li = new LevelInput("Balcony 1", 50.0, 15, 100);
		levels.add(li);
		li = new LevelInput("Balcony 2", 40.0, 15, 100);
		levels.add(li);
		EventInput ei = new EventInput(eventName, ZonedDateTime.now(), eventDuration, levels, 10);
		List<SeatLevel> seatLevels = new ArrayList<>();
		int i = 1;
		for (LevelInput levelInput : levels) {
			seatLevels.add(new SeatLevel(i++, levelInput.getName(), levelInput.getPrice(), levelInput.getRows(),
					levelInput.getSeatsInRow()));
		}
		event = new Event(1, ei.getName(), ei.getStartDateTime(), ei.getDuration(), seatLevels, ei.getHoldExpirationSeconds());
	}

	@Test
	public void testConstructor() {
		assertNotNull(event);
		assertEquals(eventName, event.getName());
		assertEquals(eventDuration, event.getDuration());
	}

	@Test
	public void testShouldGetAvailableSeats() {
		assertEquals(6250, event.getAvailableSeatCount(Optional.empty()));
	}

	@Test
	public void testShouldGetAvailableSeatsByLevels() {
		assertEquals(1250, event.getAvailableSeatCount(Optional.of(1)));
		assertEquals(2000, event.getAvailableSeatCount(Optional.of(2)));
		assertEquals(1500, event.getAvailableSeatCount(Optional.of(3)));
		assertEquals(1500, event.getAvailableSeatCount(Optional.of(4)));
	}

	@Test
	public void testShouldGetTotalAvailableSeatsForNonExistentLevel() {
		assertEquals(0, event.getAvailableSeatCount(Optional.of(5)));
	}

	@Test
	public void testShouldGetTotalSeats() {
		assertEquals(6250, event.getTotalSeatCount(Optional.empty()));
	}

	@Test
	public void testShouldGetTotalSeatsByLevels() {
		assertEquals(1250, event.getTotalSeatCount(Optional.of(1)));
		assertEquals(2000, event.getTotalSeatCount(Optional.of(2)));
		assertEquals(1500, event.getTotalSeatCount(Optional.of(3)));
		assertEquals(1500, event.getTotalSeatCount(Optional.of(4)));
	}

	@Test
	public void testShouldGetTotalSeatsForNonExistentLevel() {
		assertEquals(0, event.getTotalSeatCount(Optional.of(5)));
	}

	@Test
	public void testShouldGetSeatsGivenMinMaxLevel() {
		List<Seat> seats = event.getBestAvailableSeats(Optional.of(1), Optional.of(4), 6250);
		assertEquals(6250, seats.size());
		assertEquals(1250, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 1).count());
		assertEquals(2000, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 2).count());
		assertEquals(1500, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 3).count());
		assertEquals(1500, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 4).count());
	}

	@Test
	public void testShouldGetSeatsGivenNoLevels() {
		List<Seat> seats = event.getBestAvailableSeats(Optional.empty(), Optional.empty(), 6250);
		assertEquals(6250, seats.size());
		assertEquals(1250, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 1).count());
		assertEquals(2000, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 2).count());
		assertEquals(1500, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 3).count());
		assertEquals(1500, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 4).count());
	}

	@Test
	public void testShouldGetSeatsGivenMaxLevel() {
		List<Seat> seats = event.getBestAvailableSeats(Optional.empty(), Optional.of(2), 6250);
		assertEquals(3250, seats.size());
		assertEquals(1250, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 1).count());
		assertEquals(2000, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 2).count());
		assertEquals(0, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 3).count());
		assertEquals(0, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 4).count());
	}

	@Test
	public void testShouldGetSeatsGivenMinLevel() {
		List<Seat> seats = event.getBestAvailableSeats(Optional.of(3), Optional.empty(), 6250);
		assertEquals(3000, seats.size());
		assertEquals(0, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 1).count());
		assertEquals(0, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 2).count());
		assertEquals(1500, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 3).count());
		assertEquals(1500, seats.parallelStream().filter(seat -> seat.getId().getLevel() == 4).count());
	}

	@Test
	public void testBestSeat() {
		List<LevelInput> levels = new ArrayList<>();
		LevelInput li = new LevelInput("Orchestra", 100.0, 1, 1);
		levels.add(li);
		li = new LevelInput("Main", 75.0, 2, 2);
		levels.add(li);
		li = new LevelInput("Balcony 1", 50.0, 2, 1);
		levels.add(li);
		li = new LevelInput("Balcony 2", 40.0, 1, 1);
		levels.add(li);
		EventInput ei = new EventInput(eventName, ZonedDateTime.now(), eventDuration, levels, 10);
		List<SeatLevel> seatLevels = new ArrayList<>();
		int i = 1;
		for (LevelInput levelInput : levels) {
			seatLevels.add(new SeatLevel(i++, levelInput.getName(), levelInput.getPrice(), levelInput.getRows(),
					levelInput.getSeatsInRow()));
		}
		event = new Event(1, ei.getName(), ei.getStartDateTime(), ei.getDuration(), seatLevels, 10);
		List<Seat> seats = event.getBestAvailableSeats(Optional.empty(), Optional.empty(), 9);
		assertEquals(8, seats.size());
		List<SeatIdentifier> actualSeatOrder = seats.stream().map(seat -> seat.getId()).collect(Collectors.toList());
		List<SeatIdentifier> expectedSeatOrder = Arrays.asList(new SeatIdentifier(1, 1, 1), new SeatIdentifier(2, 1, 1),
				new SeatIdentifier(2, 1, 2), new SeatIdentifier(2, 2, 1), new SeatIdentifier(2, 2, 2),
				new SeatIdentifier(3, 1, 1), new SeatIdentifier(3, 2, 1), new SeatIdentifier(4, 1, 1));
		assertEquals(expectedSeatOrder, actualSeatOrder);
	}

	@Test
	public void testShouldCreateNewEvent() {
		List<LevelInput> levels = new ArrayList<>();
		LevelInput li = new LevelInput("Orchestra", 100.0, 1, 1);
		levels.add(li);
		li = new LevelInput("Main", 75.0, 2, 2);
		levels.add(li);
		li = new LevelInput("Balcony 1", 50.0, 2, 1);
		levels.add(li);
		li = new LevelInput("Balcony 2", 40.0, 1, 1);
		levels.add(li);
		EventInput ei = new EventInput(eventName, ZonedDateTime.now(), eventDuration, levels, 10);
		List<SeatLevel> seatLevels = new ArrayList<>();
		int i = 1;
		for (LevelInput levelInput : levels) {
			seatLevels.add(new SeatLevel(i++, levelInput.getName(), levelInput.getPrice(), levelInput.getRows(),
					levelInput.getSeatsInRow()));
		}
		event = new Event(1, ei.getName(), ei.getStartDateTime(), ei.getDuration(), seatLevels, 10);
		Map<SeatState, List<Seat>> updateSeats = new HashMap<>();
		updateSeats.put(SeatState.ON_HOLD, Arrays.asList(new Seat(new SeatIdentifier(1, 1, 1), SeatState.AVAILABLE),
				new Seat(new SeatIdentifier(4, 1, 1), SeatState.AVAILABLE)));
		updateSeats.put(SeatState.BOOKED, Collections.singletonList(new Seat(new SeatIdentifier(2, 1, 1), SeatState.AVAILABLE)));
		Event newEvent = new Event(event, updateSeats);
		List<Seat> seats = newEvent.getBestAvailableSeats(Optional.empty(), Optional.empty(), 9);
		assertEquals(5, seats.size());
		List<SeatIdentifier> actualSeatOrder = seats.stream().map(seat -> seat.getId()).collect(Collectors.toList());
		List<SeatIdentifier> expectedSeatOrder = Arrays.asList(
				new SeatIdentifier(2, 1, 2), new SeatIdentifier(2, 2, 1), new SeatIdentifier(2, 2, 2),
				new SeatIdentifier(3, 1, 1), new SeatIdentifier(3, 2, 1));
		assertEquals(expectedSeatOrder, actualSeatOrder);

		assertEquals(5, newEvent.getAvailableSeatCount(Optional.empty()));
		assertEquals(0, newEvent.getAvailableSeatCount(Optional.of(1)));
		assertEquals(3, newEvent.getAvailableSeatCount(Optional.of(2)));
		assertEquals(2, newEvent.getAvailableSeatCount(Optional.of(3)));
		assertEquals(0, newEvent.getAvailableSeatCount(Optional.of(4)));
		assertEquals(0, newEvent.getAvailableSeatCount(Optional.of(5)));

		assertEquals(8, newEvent.getTotalSeatCount(Optional.empty()));
		assertEquals(1, newEvent.getTotalSeatCount(Optional.of(1)));
		assertEquals(4, newEvent.getTotalSeatCount(Optional.of(2)));
		assertEquals(2, newEvent.getTotalSeatCount(Optional.of(3)));
		assertEquals(1, newEvent.getTotalSeatCount(Optional.of(4)));
		assertEquals(0, newEvent.getTotalSeatCount(Optional.of(5)));
	}
}
