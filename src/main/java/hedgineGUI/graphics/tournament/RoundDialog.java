package hedgineGUI.graphics.tournament;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import javax.swing.*;

import hedgineGUI.game.GameConfiguration;
import hedgineGUI.game.clock.Clock;
import hedgineGUI.game.clock.ClockBuilder;
import hedgineGUI.graphics.SwingTicker;
import hedgineGUI.tournament.FenSequence;

/** Modal dialog that collects the pairings and settings for a new series of rounds. */
final class RoundDialog extends JDialog {
    /** Participants includes the previously known ones plus everyone registered by this setup. */
    record RoundSetup(List<GameConfiguration> configurations, List<ScheduledGame> games,
            FenSequence positions, int repetitions, Map<String, File> participants,
            int randomPlies, boolean dataGeneration, int batchRounds) {
        int rounds() { return dataGeneration ? repetitions : 2 * repetitions; }
    }

    private final Map<String, File> knownParticipants;
    private final List<PairingForm> forms = new ArrayList<>();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JCheckBox randomFen = new JCheckBox("Random FEN from py/fens.txt", true);
    private final JTextField timeControl = new JTextField("X 0.2", 20);
    private final JSpinner repeat = new JSpinner(new SpinnerNumberModel(5, 1, 100_000_000, 1));
    private final JComboBox<String> mode = new JComboBox<>(new String[]{"Tournament", "Data generation"});
    private final JComboBox<String> limit = new JComboBox<>(new String[]{"Time control", "Depth"});
    private final JSpinner randomPlies = new JSpinner(new SpinnerNumberModel(4, 0, 1000, 1));
    private final JSpinner depth = new JSpinner(new SpinnerNumberModel(8, 1, 128, 1));
    private final JTextField timeout = new JTextField("10", 8);
    private final JSpinner batchRounds = new JSpinner(new SpinnerNumberModel(1000, 1, 100_000, 1));
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
        JLabel repeatLabel = new JLabel("Pairs per matchup (2 rounds each):");
        JLabel openingLabel = new JLabel("Each opening is played in both colors");
        buttons.add(repeatLabel);
        buttons.add(repeat);
        buttons.add(openingLabel);
        randomFen.setToolTipText("Reversed rounds reuse the preceding round's positions.");
        JPanel options = new JPanel(new GridLayout(0, 1));
        JPanel timeSettings = new JPanel();
        timeSettings.add(new JLabel("Time control for all games (N / X 1.5 / S 180 0 ms500)"));
        timeSettings.add(timeControl);
        JPanel modeSettings = new JPanel();
        modeSettings.add(new JLabel("Mode"));
        modeSettings.add(mode);
        JLabel limitLabel = new JLabel("Search limit");
        modeSettings.add(limitLabel);
        modeSettings.add(limit);
        options.add(modeSettings);
        options.add(timeSettings);
        JPanel depthSettings = new JPanel();
        depthSettings.add(new JLabel("Search depth N"));
        depthSettings.add(depth);
        depthSettings.add(new JLabel("Timeout per search (seconds or ms…)"));
        depthSettings.add(timeout);
        depthSettings.setVisible(false);
        options.add(depthSettings);
        JPanel generationSettings = new JPanel();
        generationSettings.add(new JLabel("Random moves K (plies)"));
        generationSettings.add(randomPlies);
        generationSettings.setVisible(false);
        options.add(generationSettings);
        JPanel batchSettings = new JPanel();
        batchSettings.add(new JLabel("Release saved history every N rounds (including retries)"));
        batchSettings.add(batchRounds);
        batchSettings.add(new JLabel("PGN is saved after every round in saves/"));
        batchSettings.setToolTipText("In tournament mode, history is only released once every pair has both games.");
        options.add(batchSettings);
        mode.addActionListener(e -> {
            boolean generation = isGeneration();
            repeatLabel.setText(generation ? "Games per matchup:" : "Pairs per matchup (2 rounds each):");
            openingLabel.setText(generation ? "One game per sampled opening" : "Each opening is played in both colors");
            randomFen.setToolTipText(generation ? "Sample a new opening for every game."
                    : "Reversed rounds reuse the preceding round's positions.");
            // Data generation is always depth limited.
            limitLabel.setVisible(!generation);
            limit.setVisible(!generation);
            timeSettings.setVisible(!isDepthLimited());
            depthSettings.setVisible(isDepthLimited());
            generationSettings.setVisible(generation);
            pack();
        });
        limit.addActionListener(e -> {
            timeSettings.setVisible(!isDepthLimited());
            depthSettings.setVisible(isDepthLimited());
            pack();
        });
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

    private boolean isGeneration() {
        return mode.getSelectedIndex() == 1;
    }

    /** in depth limited games the clock only enforces a per-search timeout. */
    private boolean isDepthLimited() {
        return isGeneration() || limit.getSelectedIndex() == 1;
    }

    private void start() {
        boolean generation = isGeneration();
        boolean depthLimited = isDepthLimited();
        String time = depthLimited ? "X " + timeout.getText().trim() : timeControl.getText().trim();
        try {
            repeat.commitEdit();
            batchRounds.commitEdit();
            randomPlies.commitEdit();
            depth.commitEdit();
            if (depthLimited) {
                Clock clock = new Clock();
                ClockBuilder.setClock(clock, time);
                if (clock.getWhiteTime() <= 0) throw new IllegalArgumentException("Timeout must be positive");
            }
            FenSequence positions = randomFen.isSelected() ? new FenSequence(Path.of("py", "fens.txt")) : null;
            // A name identifies a participant across every board and round.
            Map<String, File> identities = new HashMap<>(knownParticipants);
            List<GameConfiguration> configurations = new ArrayList<>();
            List<ScheduledGame> games = new ArrayList<>();
            for (PairingForm form : forms) {
                GameConfiguration configuration = form.configuration(identities, positions != null, time);
                if (depthLimited) {
                    configuration = new GameConfiguration(configuration.getFen(), configuration.getWhite(),
                            configuration.getBlack(), time, new SwingTicker(), (Integer) depth.getValue(), List.of());
                }
                ScheduledGame game = new ScheduledGame(configuration, form.whiteFile(), form.blackFile(), time);
                configurations.add(game.create(false));
                games.add(game);
            }
            setup = new RoundSetup(configurations, games, positions, (Integer) repeat.getValue(), identities,
                    generation ? (Integer) randomPlies.getValue() : 0, generation, (Integer) batchRounds.getValue());
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Invalid game", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }
}
