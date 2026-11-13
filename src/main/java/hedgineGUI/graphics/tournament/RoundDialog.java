package hedgineGUI.graphics.tournament;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import javax.swing.*;

import hedgineGUI.game.GameConfiguration;
import hedgineGUI.tournament.FenSequence;

/** Modal dialog that collects the pairings and settings for a new series of rounds. */
final class RoundDialog extends JDialog {
    /** Participants includes the previously known ones plus everyone registered by this setup. */
    record RoundSetup(List<GameConfiguration> configurations, List<ScheduledGame> games,
            FenSequence positions, int pairsPerMatchup, Map<String, File> participants) {}

    private final Map<String, File> knownParticipants;
    private final List<PairingForm> forms = new ArrayList<>();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JCheckBox randomFen = new JCheckBox("Random FEN from py/fens.txt (seed 1)", true);
    private final JTextField timeControl = new JTextField("X 0.2", 20);
    private final JSpinner repeat = new JSpinner(new SpinnerNumberModel(5, 1, 5000, 1));
    private RoundSetup setup;

    /** Returns null when the dialog is closed without starting. */
    static RoundSetup show(Frame owner, int round, Map<String, File> participants) {
        RoundDialog dialog = new RoundDialog(owner, round, participants);
        dialog.setVisible(true);
        return dialog.setup;
    }

    private RoundDialog(Frame owner, int round, Map<String, File> participants) {
        super(owner, "Configure round " + round, true);
        knownParticipants = participants;
        addGame();
        JPanel buttons = new JPanel();
        buttons.add(new JLabel("Pairs per matchup (2 rounds each):"));
        buttons.add(repeat);
        buttons.add(new JLabel("Each opening is played in both colors"));
        randomFen.setToolTipText("Reversed rounds reuse the preceding round's positions.");
        JPanel options = new JPanel(new GridLayout(0, 1));
        JPanel timeSettings = new JPanel();
        timeSettings.add(new JLabel("Time control for all games (N / X 1.5 / S 180 0 ms500)"));
        timeSettings.add(timeControl);
        options.add(timeSettings);
        options.add(randomFen);
        randomFen.addActionListener(e -> {
            for (PairingForm form : forms) form.setFenEnabled(!randomFen.isSelected());
        });
        JButton add = new JButton("Add game");
        add.addActionListener(e -> addGame());
        JButton remove = new JButton("Remove game");
        remove.addActionListener(e -> {
            int index = tabs.getSelectedIndex();
            if (forms.size() > 1 && index >= 0) { forms.remove(index); tabs.remove(index); }
        });
        JButton start = new JButton("Start round");
        start.addActionListener(e -> start());
        buttons.add(add); buttons.add(remove); buttons.add(start);
        add(tabs);
        options.add(buttons);
        add(options, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void addGame() {
        PairingForm form = new PairingForm();
        if (!forms.isEmpty()) form.copyFrom(forms.get(forms.size() - 1));
        form.setFenEnabled(!randomFen.isSelected());
        forms.add(form);
        tabs.addTab("Game " + forms.size(), form);
        tabs.setSelectedComponent(form);
    }

    private void start() {
        String time = timeControl.getText().trim();
        try {
            FenSequence positions = randomFen.isSelected() ? new FenSequence(Path.of("py", "fens.txt")) : null;
            // A name identifies a participant across every board and round.
            Map<String, File> identities = new HashMap<>(knownParticipants);
            List<GameConfiguration> configurations = new ArrayList<>();
            List<ScheduledGame> games = new ArrayList<>();
            for (PairingForm form : forms) {
                GameConfiguration configuration = form.configuration(identities, positions != null, time);
                configurations.add(configuration);
                games.add(new ScheduledGame(configuration, form.whiteFile(), form.blackFile(), time));
            }
            setup = new RoundSetup(configurations, games, positions, (Integer) repeat.getValue(), identities);
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Invalid game", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }
}
