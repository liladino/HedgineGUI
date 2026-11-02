package hedgineGUI.game.clock;

import java.util.ArrayList;
import java.util.List;

import hedgineGUI.utility.*;

/**
 * Represents an abstract chess clock.
 */
public final class Clock {
	/** A side may overrun its time by this much before being flagged, absorbing I/O latency. */
	public static final long FLAG_GRACE_MS = 50;

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
	// The stored times are valid as of lastUpdateMs; reads compute the elapsed part without committing it,
	// so a clock press can still be dated back to when the move actually arrived.
	private long lastUpdateMs;
	// False between pressClockAt and startTurn: the side to move is not charged until it is asked to move.
	private boolean turnStarted = true;
	private Sides flaggedSide; // null if nobody flagged

	public synchronized void setStartTime(Second startTime) {
		setStartTimeMs(1000L * startTime.time);
	}

	public synchronized void setStartTimeMs(long milliseconds) {
		whiteTimeMs = blackTimeMs = milliseconds;
		if (controlType == TimeControl.FIX_TIME_PER_MOVE) {
			moveTimeMs = whiteTimeMs;
		}
	}

	public synchronized void setControlType(TimeControl controlType) {
		this.controlType = controlType;
	}

	public synchronized void setMoveTime(Second moveTime) {
		setMoveTimeMs(1000L * moveTime.time);
	}

	public synchronized void setMoveTimeMs(long milliseconds) {
		this.moveTimeMs = milliseconds;
		this.whiteTimeMs = moveTimeMs;
		this.blackTimeMs = moveTimeMs;
	}

	public synchronized void setIncrement(Second increment, int incrementStartMove) {
		setIncrementMs(1000L * increment.time, incrementStartMove);
	}

	public synchronized void setIncrementMs(long milliseconds, int incrementStartMove) {
		this.incrementMs = milliseconds;
		this.incrementStartMove = incrementStartMove;
	}

	public synchronized void addExtraTime(int afterMove, Second addTime) {
		addExtraTimeMs(afterMove, Math.toIntExact(1000L * addTime.time));
	}

	public synchronized void addExtraTimeMs(int afterMove, int milliseconds) {
		extraTimes.add(new Pair<>(afterMove, milliseconds));
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
		turnStarted = true;
		lastUpdateMs = System.currentTimeMillis();
	}

	public synchronized void pause() {
		commitTo(System.currentTimeMillis());
		running = false;
	}

	public synchronized void resume() {
		if (controlType == TimeControl.NO_CONTROL || flaggedSide != null) {
			return;
		}
		if (!running) {
			running = true;
			turnStarted = true;
			lastUpdateMs = System.currentTimeMillis();
		}
	}

	/** Presses the clock now; the opponent's time starts running immediately. */
	public synchronized void pressClock() {
		pressClockAt(System.currentTimeMillis());
		turnStarted = true;
	}

	/**
	 * Presses the clock as of atMs, the moment the move arrived. The opponent's time
	 * is held until startTurn, so delays in between are charged to nobody.
	 */
	public synchronized void pressClockAt(long atMs) {
		commitTo(atMs);
		if (flaggedSide != null) {
			return;
		}

		applyTimeControl();

		activeSide = (activeSide == Sides.WHITE) ? Sides.BLACK : Sides.WHITE;
		plies++;
		lastUpdateMs = Math.max(lastUpdateMs, atMs);
		turnStarted = false;
	}

	/**
	 * Starts charging the side to move from atMs, when it was asked for its move.
	 * Ignored if the clock was pressed again since ply was read.
	 */
	public synchronized void startTurn(int ply, long atMs) {
		if (!running || flaggedSide != null || ply != plies) {
			return;
		}
		if (!turnStarted || atMs > lastUpdateMs) {
			lastUpdateMs = atMs;
		}
		turnStarted = true;
	}

	public synchronized int getPlyCount() {
		return plies;
	}

	public synchronized ClockSnapshot snapshot() {
		long now = System.currentTimeMillis();
		checkFlag(now);
		return new ClockSnapshot(
			controlType,
			Math.max(0, timeAt(Sides.WHITE, now)),
			Math.max(0, timeAt(Sides.BLACK, now)),
			activeSide,
			running,
			controlType != TimeControl.NO_CONTROL, // if there is time control, clock should be visible
			flaggedSide != null, //if one side is flagged, time is up
			flaggedSide,
			controlType == TimeControl.FISCHER ? incrementMs : 0,
			controlType == TimeControl.FISCHER ? incrementMs : 0
		);
	}

	/** Restores a previously emitted state, for example after a takeback. */
	public synchronized void restore(ClockSnapshot snapshot) {
		controlType = snapshot.getTimeControl();
		whiteTimeMs = snapshot.getWhiteTimeMs();
		blackTimeMs = snapshot.getBlackTimeMs();
		activeSide = snapshot.getActiveSide();
		flaggedSide = snapshot.getFlaggedSide();
		running = snapshot.isRunning() && flaggedSide == null
				&& controlType != TimeControl.NO_CONTROL;
		turnStarted = true;
		lastUpdateMs = System.currentTimeMillis();
	}

	/**
	 * getters for testing
	 */
	public synchronized long getWhiteTime() {
		long now = System.currentTimeMillis();
		checkFlag(now);
		return Math.max(0, timeAt(Sides.WHITE, now));
	}
	public synchronized long getBlackTime() {
		long now = System.currentTimeMillis();
		checkFlag(now);
		return Math.max(0, timeAt(Sides.BLACK, now));
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

	/** Time charged to the side to move since lastUpdateMs, as of timeMs. */
	private long elapsedAt(long timeMs) {
		if (!running || !turnStarted || controlType == TimeControl.NO_CONTROL || flaggedSide != null) {
			return 0;
		}
		return Math.max(0, timeMs - lastUpdateMs);
	}

	private long timeAt(Sides side, long timeMs) {
		long stored = side == Sides.WHITE ? whiteTimeMs : blackTimeMs;
		return side == activeSide ? stored - elapsedAt(timeMs) : stored;
	}

	/** Flags the side to move once it is past the grace period. */
	private void checkFlag(long timeMs) {
		if (timeAt(activeSide, timeMs) <= -FLAG_GRACE_MS) {
			commitTo(timeMs);
		}
	}

	/** Deducts the elapsed time up to timeMs from the side to move. */
	private void commitTo(long timeMs) {
		long elapsed = elapsedAt(timeMs);
		if (elapsed > 0) {
			// Time may run negative within the grace period; it is shown as 0.
			if (activeSide == Sides.WHITE) {
				whiteTimeMs = Math.max(-FLAG_GRACE_MS, whiteTimeMs - elapsed);
				if (whiteTimeMs <= -FLAG_GRACE_MS) {
					flaggedSide = Sides.WHITE;
				}
			} else {
				blackTimeMs = Math.max(-FLAG_GRACE_MS, blackTimeMs - elapsed);
				if (blackTimeMs <= -FLAG_GRACE_MS) {
					flaggedSide = Sides.BLACK;
				}
			}
		}
		lastUpdateMs = Math.max(lastUpdateMs, timeMs);
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
