package game.interfaces;

import utility.Sides;

/**
 * Clock notifies the TimePanels this way
 */
public interface ClockListener {
	void updateClock(Sides side, long whiteTime, long blackTime);
	void stoppedClock();
	void hideTimer();
	void showTimer();
}
