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

	public void setPlyCount(int plies) {
		clock.setPlyCount(plies);
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
		clock.start();
		isRunning = clock.isRunning();
		if (isRunning) {
			ticker.start(this::onTick);
		}
	}

	public void pauseClock() {
		isRunning = false;
		clock.pause();
		ticker.stop();
	}

	public void resumeClock() {
		clock.resume();
		isRunning = clock.isRunning();
		if (isRunning) {
			ticker.start(this::onTick);
		}
	}

	public void pressClock() {
		clock.pressClock();
	}

	public ClockSnapshot snapshot() {
		return clock.snapshot();
	}

	public void restore(ClockSnapshot snapshot) {
		ticker.stop();
		clock.restore(snapshot);
		isRunning = clock.isRunning();
		if (isRunning) {
			ticker.start(this::onTick);
		}
	}
}
