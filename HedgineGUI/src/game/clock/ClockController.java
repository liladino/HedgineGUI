package game.clock;

import java.util.ArrayList;
import java.util.List;

import utility.Sides;

public class ClockController {
	private Clock clock;
	private List<ClockListener> listeners;
	private Ticker ticker;
	private boolean isRunning;

	public ClockController(Ticker t, Clock c){
		ticker = t;
		clock = c;
		listeners = new ArrayList<>();
		isRunning = false;
	}

	// Clock can be built from time information via the ClockBuilder
	public void setClock(Clock c){
		clock = c;
	}
	
    public void setActiveSide(Sides s){
		clock.setActiveSide(s);
	}

	public void subscribe(ClockListener listener){
		listeners.add(listener);
	}

	private void onTick() {
		ClockSnapshot snapshot = clock.snapshot();

		if (snapshot.isTimeUp()) {
			if (isRunning) {
				pauseClock();
				for (ClockListener l : listeners){
					l.onTimeUp(snapshot);
				}
			}
		}
        else {
			for (ClockListener l : listeners){
				l.onTick(snapshot);
            }
        }
	}

	public void startClock() {
		isRunning = true;
		clock.start();
        ticker.start(() -> this.onTick());
	}

	public void pauseClock() {
		isRunning = false;
		clock.pause();
		ticker.stop();
	}

	public void resumeClock() {
		isRunning = true;
		clock.resume();
		ticker.start(() -> this.onTick());
	}

	public void pressClock() {
		clock.pressClock();
	}

	public ClockSnapshot snapshot() {
		return clock.snapshot();
	}
}
