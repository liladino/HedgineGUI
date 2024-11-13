package clockParsing;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.Clock;
import game.GameManager;
import game.TimeInformationConverter;
import utility.Pair;
import utility.Second;
import utility.TimeControl;

public class TimeInformationConverterTest {
	Clock clock;
	TimeInformationConverter t;
	@BeforeEach
	void init(){
		GameManager gameManager = new GameManager();
		clock = gameManager.getClock();
		t = new TimeInformationConverter();
	}

	@Test
	void noControl() {
		t.setNoControl();
		assertDoesNotThrow(() -> {t.setClock(clock);});
		assertEquals(TimeControl.NO_CONTROL, clock.getTimeControl());
	}

	@Test
	void fixTimeControl() {
		t.setFixTime(new Second(5));
		assertDoesNotThrow(() -> {t.setClock(clock);});
		assertEquals(TimeControl.FIX_TIME_PER_MOVE, clock.getTimeControl());
		assertEquals(5000, clock.getWhiteTime());
		assertEquals(5000, clock.getBlackTime());
	}

	@Test
	void fischerControl() {
		t.setFischer(new Second(5), 10, new Second(3));
		t.addExtraTime(40, new Second(30 * 60));
		t.addExtraTime(60, new Second(15 * 60));		
		
		assertDoesNotThrow(() -> {t.setClock(clock);});
		assertEquals(TimeControl.FISCHER, clock.getTimeControl());
		assertEquals(5000, clock.getWhiteTime());
		assertEquals(5000, clock.getBlackTime());
		assertEquals(3000, clock.getIncrement());
		assertEquals(10, clock.getIncrementStartMove());

		List<Pair<Integer, Integer>> extraTimes = clock.getExtraTime();
		assertEquals(40, extraTimes.get(0).first);
		assertEquals(30 * 60 * 1000, extraTimes.get(0).second);
		assertEquals(60, extraTimes.get(1).first);
		assertEquals(15 * 60 * 1000, extraTimes.get(1).second);

	}
}
