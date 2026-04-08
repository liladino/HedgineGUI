package core.clock;

import java.awt.event.*;
import javax.swing.Timer;

public class SwingTimer implements Ticker, ActionListener {
	private Timer timer;
	private Runnable tick;

	public SwingTimer(){
		tick = null;
		timer = new Timer(100, this);
	}

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
	public void actionPerformed(ActionEvent e) {
		if (null != tick){
			tick.run();
		}
	}
		
}