package core.clock;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import utility.Sides;

public class ClockController {
	private Clock clock;
	private List<ClockListener> listeners;
	private Ticker ticker;

	public ClockController(Ticker t, Clock c){
		ticker = t;
		clock = c;
		listeners = new ArrayList<>();
	}

	public ClockController(Ticker t){
        this(t, null);
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
			for (ClockListener l : listeners){
				l.onTimeUp(snapshot);
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
        ticker.start(() -> this.onTick());
	}

	public void pauseClock() {
		clock.pause();
	}

	public void resumeClock() {
		clock.resume();
	}

	public void pressClock() {
		clock.pressClock();
	}

	public ClockSnapshot snapshot() {
		return clock.snapshot();
	}
}
