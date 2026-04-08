package core.clock;

import java.util.ArrayList;
import java.util.List;

public class ClockController {
	private Clock clock;
	private List<IClockListener> listeners;
	private ITicker ticker;

	public ClockController(ITicker t, Clock c){
		ticker = t;
		clock = c;
		listeners = new ArrayList<>();
	}

	/** 
	 * Clock can be built from time information via the ClockBuilder
	 */
	public void setClock(Clock c){
		clock = c;
	}

	public void subscribe(IClockListener listener){
		listeners.add(listener);	
	}

	private void onTick() {
		ClockSnapshot snapshot = clock.snapshot();

		if (snapshot.isTimeUp()) {
			for (IClockListener l : listeners){
				l.onTimeUp(snapshot);
			}
		}
        else {
			for (IClockListener l : listeners){
				l.onTick(snapshot);
            }
        }
	}

	public void startClock() {
		clock.start();
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
