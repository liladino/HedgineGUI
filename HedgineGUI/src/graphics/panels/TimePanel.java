package graphics.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;

import core.ClockSnapshot;

import java.awt.Color;
import java.awt.Font;

import utility.Sides;

/**
 * Shows the remaining time for one side. 
 * Formats it as minutes:seconds, or seconds:centiseconds if less then 30 seconds remain
 */
public class TimePanel extends JPanel {
    private static final long serialVersionUID = 8502136930868216129L;

    private final Sides side;
    private final JLabel timeLabel;

    public TimePanel(Sides side) {
        this.side = side;
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Courier new", Font.BOLD, 20));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setBackground(Color.WHITE);
        updateTime(0);
        add(timeLabel);
    }

    public void render(ClockSnapshot snapshot) {
        setVisible(snapshot.isVisible());

        boolean active = snapshot.isRunning() && snapshot.getActiveSide() == side;
        timeLabel.setForeground(active ? Color.BLACK : Color.GRAY);

        long time = (side == Sides.WHITE)
            ? snapshot.getWhiteTimeMs()
            : snapshot.getBlackTimeMs();

        updateTime(time);
        repaint();
    }

    public void updateTime(long time) {
        timeLabel.setText(formatTime(time));
    }

    private String formatTime(long time) {
        long minutes = time / 60000;
        long seconds = (time / 1000) % 60;
        long centiSeconds = (time - minutes * 60000 - seconds * 1000) / 10;

        if (time > 30_000) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d:%02d", seconds, centiSeconds);
    }
}