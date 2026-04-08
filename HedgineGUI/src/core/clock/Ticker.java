package core.clock;

public interface Ticker {
	void start(Runnable tick);
    void stop();
}
