package graphics.panels;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import control.GameController;
import control.GameState;
import utility.Sides;

/** Player names, clocks and move text rendered solely from GameState. */
public final class RightPanel extends JPanel {
    private static final long serialVersionUID = 3474454907485110512L;

    private final JTextArea whiteName = playerNameArea("White Player");
    private final JTextArea blackName = playerNameArea("Black Player");
    private final JTextArea movesArea = new JTextArea();
    private final TimePanel whiteClock = new TimePanel(Sides.WHITE);
    private final TimePanel blackClock = new TimePanel(Sides.BLACK);

    public RightPanel(GameController controller) {
        setPreferredSize(new Dimension(300, getHeight()));
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 20, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0.05;
        constraints.weightx = 0.5;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(whiteName, constraints);

        constraints.gridx = 1;
        add(blackName, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        add(whiteClock, constraints);

        constraints.gridx = 1;
        add(blackClock, constraints);

        movesArea.setFont(new Font("Courier new", Font.PLAIN, 16));
        movesArea.setLineWrap(true);
        movesArea.setWrapStyleWord(true);
        movesArea.setEditable(false);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        add(new JScrollPane(movesArea), constraints);

        controller.addStateListener(this::receiveState);
    }

    private static JTextArea playerNameArea(String initialText) {
        JTextArea area = new JTextArea(initialText);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setFont(new Font("Courier new", Font.BOLD, 17));
        return area;
    }

    private void receiveState(GameState state) {
        Runnable render = () -> {
            whiteName.setText(state.getWhiteName());
            blackName.setText(state.getBlackName());
            movesArea.setText(state.getMoveText());
            whiteClock.render(state.getClock());
            blackClock.render(state.getClock());
        };
        if (SwingUtilities.isEventDispatchThread()) {
            render.run();
        } else {
            SwingUtilities.invokeLater(render);
        }
    }
}
