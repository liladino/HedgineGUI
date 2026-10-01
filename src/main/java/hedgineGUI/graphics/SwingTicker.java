package graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import game.clock.Ticker;

/** Swing/EDT adapter for the game-layer Ticker port. */
public final class SwingTicker implements Ticker, ActionListener {
    private final Timer timer = new Timer(100, this);
    private Runnable tick;

    @Override
    public void start(Runnable tick) {
        this.tick = tick;
        timer.start();
    }

    @Override
    public void stop() {
        timer.stop();
        tick = null;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Runnable currentTick = tick;
        if (currentTick != null) {
            currentTick.run();
        }
    }
}
