package game.clock;

public class HeadlessTicker implements Ticker {
	private Runnable tick;

	public HeadlessTicker(){ }

	public void simulateTick(){
		tick.run();
	}

	@Override
	public void start(Runnable tick) {
		this.tick = tick;
	}

	@Override
	public void stop() {
		this.tick = null;
	}
}