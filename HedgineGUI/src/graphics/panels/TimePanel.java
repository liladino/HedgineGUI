package graphics.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Font;

import game.interfaces.ClockListener;
import utility.Sides;

public class TimePanel extends JPanel implements ClockListener {
	private Sides side; 
	private JLabel timeLabel;
	public TimePanel(Sides side){
		this.side = side;
		timeLabel = new JLabel();
		timeLabel.setFont(new Font("Courier new", Font.BOLD, 20));
		timeLabel.setForeground(Color.GRAY);
		timeLabel.setBackground(Color.WHITE);
		updateTime(0);
		add(timeLabel);
	}

	public void updateTime(long time) {
		timeLabel.setText(formatTime(time));
	}

	private String formatTime(long time) {
		long minutes = time / 60000;
		long seconds = (time / 1000) % 60;
		long centiSeconds = (time - minutes * 60000 - seconds * 1000) / 10;
		String r;
		if (time > 30 * 1000) r = String.format("%02d:%02d", minutes, seconds);
		else r = String.format("%02d:%02d", seconds, centiSeconds);
		return r;
	}

	@Override
	public void updateClock(Sides side, long whiteTime, long blackTime){
		if (this.side == side) {
			timeLabel.setForeground(Color.BLACK);
		}
		else {
			timeLabel.setForeground(Color.GRAY);
		}
		if (this.side == Sides.BLACK) updateTime(blackTime);
		else updateTime(whiteTime);
		repaint();
	}

	@Override
	public void stoppedClock() {
		timeLabel.setForeground(Color.GRAY);
        repaint();
	}
	
}
