package game.interfaces;

import utility.Sides;

public interface ClockListener {
	void updateClock(Sides side, long whiteTime, long blackTime);
	void stoppedClock();
}
