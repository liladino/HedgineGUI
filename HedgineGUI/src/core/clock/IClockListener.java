package core.clock;

public interface IClockListener {
	void onTick(ClockSnapshot snapshot);
	void onTimeUp(ClockSnapshot snapshot);

	// void updateClock(Sides side, long whiteTime, long blackTime);
	// void stoppedClock();
	// void hideTimer();
	// void showTimer();
}
