package core.clock;

import java.util.ArrayList;
import java.util.List;

import utility.*;

/*
public class Clock implements Runnable, GameEventListener {
	private TimeControl controlType;
	private int whiteTime; 
	private int blackTime;
	private int increment; 
	private int incrementStartMove; //e.g. increment comes after the 40th move

	private int plies; //how many times the clock was pressed
	private int moveTime; 
	private boolean isWhiteActive;
	private ArrayList<Pair<Integer, Integer>> extraTimes; //after move X give players Y time (moveCount, extraTime)
	
	private TimeEventListener timeEventListener;
	private ClockListener whiteClockPanel;
	private ClockListener blackClockPanel;

	private boolean clockWasPressed;
	private boolean timerUp; // signal for time expiration
	private boolean ticking; // tracks if the timer should be ticking
	private volatile boolean gameEnded;

	public Clock(GameManager gameManager){
		controlType = TimeControl.NO_CONTROL;
		extraTimes = new ArrayList<>();
		isWhiteActive = true;
		plies = 0;
		increment = 0;
		incrementStartMove = 0;
		gameEnded = timerUp = gameEnded = ticking = false;
		gameManager.addGameChangeListener(this);
	}
	@Override
	public void run() {
		updateDisplay();
		long startTime = System.currentTimeMillis();
		while (!timerUp && !gameEnded) {
			int tikRateMillis = 50;
			synchronized (this){
				clockWasPressed = false;
				try {
					wait(tikRateMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			long elapsed = System.currentTimeMillis() - startTime;
			
			if (ticking) {
				if (isWhiteActive) {
					whiteTime -= (int)elapsed;
					if (whiteTime <= 0) {
						timerUp = true;
						whiteTime = 0;
					}
				} else {
					blackTime -= (int)elapsed;
					if (blackTime <= 0) {
						timerUp = true;
						blackTime = 0;
					}
				}
				if (timerUp){
					updateDisplay();
					signalTimeIsUp();
					timerUp = false;
					break;
				}
				else if (clockWasPressed){
					updateClockData();
				}
				updateDisplay();
			}
			startTime = System.currentTimeMillis();
		}
	}
   	 
	private void updateClockData(){
		if (controlType == TimeControl.FIX_TIME_PER_MOVE){
			blackTime = whiteTime = moveTime;
		}
		else if (controlType == TimeControl.FISCHER){
			if (plies/2 > incrementStartMove){
				if (isWhiteActive) whiteTime += increment;
				else blackTime += increment;
			}
			for (Pair<Integer, Integer> p : extraTimes){
				if (plies/2 == p.first){
					if (isWhiteActive) whiteTime += p.second;
					else blackTime += p.second;
				}
			}
		}
		isWhiteActive = !isWhiteActive;

		plies++;
	}
}*/

/**
 * Represents an abstract chess clock.
 */
public final class Clock {
	private TimeControl controlType = TimeControl.NO_CONTROL;

	private long whiteTimeMs;
	private long blackTimeMs;

	private long moveTimeMs;
	private long incrementMs;
	private int incrementStartMove;

	private int plies;
	private Sides activeSide = Sides.WHITE;

	private final List<Pair<Integer, Integer>> extraTimes = new ArrayList<>();

	private boolean running;
	private long lastUpdateMs;
	private Sides flaggedSide; // null if nobody flagged

	public synchronized void setStartTime(Second startTime) {
		whiteTimeMs = blackTimeMs = 1000L * startTime.time;
		if (controlType == TimeControl.FIX_TIME_PER_MOVE) {
			moveTimeMs = whiteTimeMs;
		}
	}

	public synchronized void setControlType(TimeControl controlType) {
		this.controlType = controlType;
	}

	public synchronized void setMoveTime(Second moveTime) {
		this.moveTimeMs = 1000L * moveTime.time;
		this.whiteTimeMs = moveTimeMs;
		this.blackTimeMs = moveTimeMs;
	}

	public synchronized void setIncrement(Second increment, int incrementStartMove) {
		this.incrementMs = 1000L * increment.time;
		this.incrementStartMove = incrementStartMove;
	}

	public synchronized void addExtraTime(int afterMove, Second addTime) {
		extraTimes.add(new Pair<>(afterMove, 1000 * addTime.time));
	}

	public synchronized void setPlyCount(int plies) {
		this.plies = plies;
	}

	public synchronized void setActiveSide(Sides side) {
		this.activeSide = side;
	}

	public synchronized void start() {
		if (controlType == TimeControl.NO_CONTROL) {
			running = false;
			return;
		}
		running = true;
		lastUpdateMs = System.currentTimeMillis();
	}

	public synchronized void pause() {
		advanceToNow();
		running = false;
	}

	public synchronized void resume() {
		if (controlType == TimeControl.NO_CONTROL || flaggedSide != null) {
			return;
		}
		if (!running) {
			running = true;
			lastUpdateMs = System.currentTimeMillis();
		}
	}

	public synchronized void pressClock() {
		advanceToNow();
		if (flaggedSide != null) {
			return; // flaggedSide;
		}

		applyTimeControl();

		activeSide = (activeSide == Sides.WHITE) ? Sides.BLACK : Sides.WHITE;
		plies++;
		lastUpdateMs = System.currentTimeMillis();
		// return null;
	}

	public synchronized ClockSnapshot snapshot() {
		advanceToNow();
		return new ClockSnapshot(
			whiteTimeMs,
			blackTimeMs,
			activeSide,
			running,
			controlType != TimeControl.NO_CONTROL, // if there is time control, clock should be visible
			flaggedSide != null, //if one side is flagged, time is up
			flaggedSide
		);
	}

	/**
	 * getters for testing
	 */
	public synchronized long getWhiteTime() {
		advanceToNow();
		return whiteTimeMs;
	}

	public synchronized long getBlackTime() {
		advanceToNow();
		return blackTimeMs;
	}

	public synchronized int getIncrementStartMove() {
		return incrementStartMove;
	}

	public synchronized int getIncrement() {
		return (int) incrementMs;
	}

	public synchronized TimeControl getTimeControl() {
		return controlType;
	}

	public synchronized Sides activeSide() {
		return activeSide;
	}

	public List<Pair<Integer, Integer>> getExtraTime(){
		return extraTimes;
	}

    public boolean isRunning(){
        return running;
    }

	/**
	 * Advances the clock to the current time, keeps the internal state up to sync if needed.
	 */
	private void advanceToNow() {
		if (!running || controlType == TimeControl.NO_CONTROL || flaggedSide != null) {
			return;
		}

		long now = System.currentTimeMillis();
		long elapsed = now - lastUpdateMs;
		if (elapsed <= 0) {
			return;
		}

		if (activeSide == Sides.WHITE) {
			whiteTimeMs = Math.max(0, whiteTimeMs - elapsed);
			if (whiteTimeMs <= 0) {
				flaggedSide = Sides.WHITE;
			}
		} else {
			blackTimeMs = Math.max(0, blackTimeMs - elapsed);
			if (blackTimeMs <= 0) {
				flaggedSide = Sides.BLACK;
			}
		}

		lastUpdateMs = now;
	}

	private void applyTimeControl() {
		if (controlType == TimeControl.FIX_TIME_PER_MOVE) {
			whiteTimeMs = moveTimeMs;
			blackTimeMs = moveTimeMs;
			return;
		}

		if (controlType == TimeControl.FISCHER) {
			if (plies / 2 > incrementStartMove) {
				if (activeSide == Sides.WHITE) {
					whiteTimeMs += incrementMs;
				} else {
					blackTimeMs += incrementMs;
				}
			}

			for (Pair<Integer, Integer> p : extraTimes) {
				if (plies / 2 == p.first) {
					if (activeSide == Sides.WHITE) {
						whiteTimeMs += p.second;
					} else {
						blackTimeMs += p.second;
					}
				}
			}
		}
	}
}