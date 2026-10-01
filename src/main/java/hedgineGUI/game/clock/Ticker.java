package hedgineGUI.game.clock;

public interface Ticker {
	void start(Runnable tick);
    void stop();
}
