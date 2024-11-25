package game.interfaces;

import utility.Sides;

/**
 * The Clock signals to the GameManager if the time is up
 */
public interface TimeEventListener {
	void onTimeIsUp(Sides active);
}
