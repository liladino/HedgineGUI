package game.clock;

public interface ClockListener {
	void onTick(ClockSnapshot snapshot);
	void onTimeUp(ClockSnapshot snapshot);
}
