package clocktest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hedgineGUI.game.clock.Clock;
import hedgineGUI.game.clock.ClockBuilder;
import hedgineGUI.game.clock.TimeInputException;
import hedgineGUI.utility.Pair;
import hedgineGUI.utility.Second;
import hedgineGUI.utility.TimeControl;

class ClockBuilderTest {
	Clock clock;
	ClockBuilder t;
	@BeforeEach
	void init(){
		clock = new Clock();
		t = new ClockBuilder();
	}

	@Test
	void noControl() {
		t.setNoControl();
		assertDoesNotThrow(() -> t.setClock(clock));
		assertEquals(TimeControl.NO_CONTROL, clock.getTimeControl());
	}

	@Test
	void fixTimeControl() {
		t.setFixTime(new Second(5));
		assertDoesNotThrow(() -> t.setClock(clock));
		assertEquals(TimeControl.FIX_TIME_PER_MOVE, clock.getTimeControl());
		assertEquals(5000, clock.getWhiteTime());
		assertEquals(5000, clock.getBlackTime());
		
		assertDoesNotThrow(() -> ClockBuilder.setClock(clock, "X 10 1 1"));
	}

	@Test
	void fischerControl() {
		t.setFischer(new Second(5), 10, new Second(3));
		t.addExtraTime(40, new Second(30 * 60));
		t.addExtraTime(60, new Second(15 * 60));		
		
		assertDoesNotThrow(() -> t.setClock(clock));
		assertEquals(TimeControl.FISCHER, clock.getTimeControl());
		assertEquals(5000, clock.getWhiteTime());
		assertEquals(5000, clock.getBlackTime());
		assertEquals(3000, clock.getIncrement());
		assertEquals(10, clock.getIncrementStartMove());

		List<Pair<Integer, Integer>> extraTimes = clock.getExtraTime();
		assertEquals(2, extraTimes.size());
		assertEquals(40, extraTimes.get(0).first);
		assertEquals(30 * 60 * 1000, extraTimes.get(0).second);
		assertEquals(60, extraTimes.get(1).first);
		assertEquals(15 * 60 * 1000, extraTimes.get(1).second);

	}

	@Test
	void fischerControl2() {
		assertDoesNotThrow(() -> ClockBuilder.setClock(clock, "S 5"));
		assertEquals(TimeControl.FISCHER, clock.getTimeControl());
		assertEquals(5000, clock.getWhiteTime());
		assertEquals(5000, clock.getBlackTime());
		assertEquals(0, clock.getIncrement());

		List<Pair<Integer, Integer>> extraTimes = clock.getExtraTime();
		assertEquals(0, extraTimes.size());
	}

	@Test
	void fischerControl3() {
		assertThrows(TimeInputException.class, () -> ClockBuilder.setClock(clock, ""));
		assertThrows(TimeInputException.class, () -> ClockBuilder.setClock(clock, " "));
		assertThrows(TimeInputException.class, () -> ClockBuilder.setClock(clock, "\t"));
		assertThrows(TimeInputException.class, () -> ClockBuilder.setClock(clock, "S"));
		assertThrows(TimeInputException.class, () -> ClockBuilder.setClock(clock, "S a"));
		assertThrows(TimeInputException.class, () -> ClockBuilder.setClock(clock, "C 1"));
	}

	@Test
	void fischerControl4() {
		assertDoesNotThrow(() -> ClockBuilder.setClock(clock, "S 180 0 2 15 15 1"));
		assertEquals(TimeControl.FISCHER, clock.getTimeControl());
		assertEquals(180*1000, clock.getWhiteTime());
		assertEquals(180*1000, clock.getBlackTime());
		assertEquals(2000, clock.getIncrement());
		assertEquals(0, clock.getIncrementStartMove());

		List<Pair<Integer, Integer>> extraTimes = clock.getExtraTime();
		assertEquals(1, extraTimes.size());
		assertEquals(15, extraTimes.get(0).first);
		assertEquals(15 * 1000, extraTimes.get(0).second);
	}

    @Test
    void acceptsDecimalSecondsAndMillisecondsEverywhere() throws Exception {
        ClockBuilder.setClock(clock, "  S  1.5  0  ms500  40  2.125  ");
        assertEquals(1500, clock.getWhiteTime());
        assertEquals(500, clock.getIncrement());
        assertEquals(40, clock.getExtraTime().get(0).first);
        assertEquals(2125, clock.getExtraTime().get(0).second);
        Clock other = new Clock();
        ClockBuilder.setClock(other, "S ms1500 0 0.5 40 ms2125");
        assertEquals(clock.getWhiteTime(), other.getWhiteTime());
        assertEquals(clock.getIncrement(), other.getIncrement());
        assertEquals(clock.getExtraTime().get(0).second, other.getExtraTime().get(0).second);
    }

    @Test
    void acceptsFractionalFixedMoveTime() throws Exception {
        ClockBuilder.setClock(clock, "X 0.5");
        assertEquals(500, clock.getWhiteTime());
        ClockBuilder.setClock(clock, "X ms500");
        assertEquals(500, clock.getBlackTime());
    }

    @Test
    void rejectsInvalidDurationsAndFractionalMoveNumbersWithoutChangingClock() throws Exception {
        ClockBuilder.setClock(clock, "X 2");
        for (String input : List.of("X -1", "X NaN", "X Infinity", "X ms",
                "X ms1.5", "X 0.0001", "X 999999999999999999999", "X 1,5",
                "S 1 0.5 1", "S 1 ms2 1", "S 1 0 1 40.5 2")) {
            assertThrows(TimeInputException.class, () -> ClockBuilder.setClock(clock, input), input);
            assertEquals(2000, clock.getWhiteTime());
            assertEquals(TimeControl.FIX_TIME_PER_MOVE, clock.getTimeControl());
        }
    }
}
