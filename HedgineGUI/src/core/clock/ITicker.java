package core.clock;

public interface ITicker {
	void start(std::function<void()> tick);
    void stop();
}
