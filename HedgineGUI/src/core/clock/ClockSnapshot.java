package core.clock;

import utility.Sides;
	
public final class ClockSnapshot {
	private final long whiteTimeMs;
	private final long blackTimeMs;
	private final Sides activeSide;
	private final boolean running;
	private final boolean visible;
	private final boolean timeUp;
	private final Sides flaggedSide; // null if nobody flagged

	public ClockSnapshot(long whiteTimeMs, long blackTimeMs, Sides activeSide, boolean running, boolean visible, boolean timeUp, Sides flaggedSide) {
		this.whiteTimeMs = whiteTimeMs;
		this.blackTimeMs = blackTimeMs;
		this.activeSide = activeSide;
		this.running = running;
		this.visible = visible;
		this.timeUp = timeUp;
		this.flaggedSide = flaggedSide;
	}

	public long getWhiteTimeMs() { return whiteTimeMs; }
	public long getBlackTimeMs() { return blackTimeMs; }
	public Sides getActiveSide() { return activeSide; }
	public boolean isRunning() { return running; }
	public boolean isVisible() { return visible; }
	public boolean isTimeUp() { return timeUp; }
	public Sides getFlaggedSide() { return flaggedSide; }
}
