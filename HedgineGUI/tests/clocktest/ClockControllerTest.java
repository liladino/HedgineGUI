package clocktest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.ClockBuilder;
import core.clock.Clock;
import core.clock.ClockController;
import core.clock.ClockListener;
import core.clock.ClockSnapshot;
import core.clock.HeadlessTicker;
import utility.Sides;

public class ClockControllerTest implements ClockListener {
	ClockController controller;
	Clock clock;
	HeadlessTicker ticker;
	int ticks;
	boolean timeUp;
	
	@BeforeEach
	void init(){
		clock = new Clock();
		ticker = new HeadlessTicker();
		controller = new ClockController(ticker, clock);
		timeUp = false;
		ticks = 0;

		assertDoesNotThrow(() -> ClockBuilder.setClock(clock, "S 1"));
		controller.setActiveSide(Sides.WHITE);
		assertEquals(Sides.WHITE, clock.activeSide());
		assertEquals(1000, clock.getBlackTime());
	}

	@Test
	void testClockPress() {		
		//start white
		controller.startClock();
		assertEquals(true, clock.isRunning());

		//white moves
		assertDoesNotThrow(() -> Thread.sleep(600));
		controller.pressClock();
		assertEquals(Sides.BLACK, clock.activeSide());
		assertEquals(false, controller.snapshot().isTimeUp());
		assertEquals(null, controller.snapshot().getFlaggedSide());

		//black moves
		assertDoesNotThrow(() -> Thread.sleep(600));
		controller.pressClock();

		assertEquals(false, controller.snapshot().isTimeUp());
		assertEquals(null, controller.snapshot().getFlaggedSide());

		//time up
		assertDoesNotThrow(() -> Thread.sleep(600));
		
		assertEquals(Sides.WHITE, controller.snapshot().getFlaggedSide());
		assertEquals(true, controller.snapshot().isTimeUp());
	}

	@Test
	void testTimeUp() {		
		controller.subscribe(this);
		controller.startClock();
		for (int i = 1; i < 10; i++){
			ticker.simulateTick();
			assertEquals(i, ticks);
			assertEquals(false, timeUp);
		}
		assertDoesNotThrow(() -> Thread.sleep(600));
		ticker.simulateTick();
		assertEquals(false, timeUp);

		assertDoesNotThrow(() -> Thread.sleep(600));
		ticker.simulateTick();
		assertEquals(Sides.WHITE, controller.snapshot().getFlaggedSide());
		assertEquals(true, controller.snapshot().isTimeUp());
		assertEquals(true, timeUp);
	}

	@Override
	public void onTick(ClockSnapshot snapshot) {
		ticks++;
	}

	@Override
	public void onTimeUp(ClockSnapshot snapshot) {
		timeUp = true;
		ticks++;
	}
}
