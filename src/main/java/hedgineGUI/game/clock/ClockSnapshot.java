package game.clock;

import utility.Sides;
import utility.TimeControl;
	
public final class ClockSnapshot {
	private final TimeControl timeControl;
	private final long whiteTimeMs;
	private final long blackTimeMs;
	private final Sides activeSide;
	private final boolean running;
	private final boolean visible;
	private final boolean timeUp;
	private final Sides flaggedSide; // null if nobody flagged
	private final long winc;
	private final long binc;

	public ClockSnapshot(TimeControl timeControl, long whiteTimeMs, long blackTimeMs, Sides activeSide, 
						boolean running, boolean visible, boolean timeUp, Sides flaggedSide, long winc, long binc) {
		this.timeControl = timeControl;
		this.whiteTimeMs = whiteTimeMs;
		this.blackTimeMs = blackTimeMs;
		this.activeSide = activeSide;
		this.running = running;
		this.visible = visible;
		this.timeUp = timeUp;
		this.flaggedSide = flaggedSide;
		this.winc = winc;
		this.binc = binc;
	}

	public ClockSnapshot(TimeControl timeControl, long whiteTimeMs, long blackTimeMs, Sides activeSide, 
						boolean running, boolean visible, boolean timeUp, Sides flaggedSide) {
		this.timeControl = timeControl;
		this.whiteTimeMs = whiteTimeMs;
		this.blackTimeMs = blackTimeMs;
		this.activeSide = activeSide;
		this.running = running;
		this.visible = visible;
		this.timeUp = timeUp;
		this.flaggedSide = flaggedSide;
		this.winc = 0;
		this.binc = 0;
	}

	public ClockSnapshot(TimeControl timeControl, long whiteTimeMs, long blackTimeMs, Sides activeSide, 
						boolean running, boolean visible, boolean timeUp) {
		this.timeControl = timeControl;
		this.whiteTimeMs = whiteTimeMs;
		this.blackTimeMs = blackTimeMs;
		this.activeSide = activeSide;
		this.running = running;
		this.visible = visible;
		this.timeUp = timeUp;
		this.flaggedSide = null;
		this.winc = 0;
		this.binc = 0;
	}

	public TimeControl getTimeControl() { return timeControl; }
	public long getWhiteTimeMs() { return whiteTimeMs; }
	public long getBlackTimeMs() { return blackTimeMs; }
	public long getBincMs() { return binc; }
	public long getWincMs() { return winc; }
	public Sides getActiveSide() { return activeSide; }
	public boolean isRunning() { return running; }
	public boolean isVisible() { return visible; }
	public boolean isTimeUp() { return timeUp; }
	public Sides getFlaggedSide() { return flaggedSide; }
}
