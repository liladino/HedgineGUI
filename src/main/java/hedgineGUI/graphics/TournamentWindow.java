package hedgineGUI.graphics;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import hedgineGUI.control.*;
import hedgineGUI.game.*;
import hedgineGUI.graphics.chessBoard.ChessBoardPanel;
import hedgineGUI.graphics.dialogs.EngineFileChooser;
import hedgineGUI.tournament.Standings;
import hedgineGUI.tournament.PairStatistics;
import hedgineGUI.tournament.FenSequence;
import hedgineGUI.utility.Sides;

/** Manual pairings, concurrent games per round, and a persistent tournament history. */
public final class TournamentWindow extends JFrame {
    private final List<GameController> games = new ArrayList<>();
    private final List<GameController> attemptedGames = new ArrayList<>();
    private final Map<GameController, String> startupErrors = new HashMap<>();
    private final DefaultTableModel resolutions = new DefaultTableModel(
            new String[]{"Resolution", "This round", "Tournament"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final List<GameController> currentRound = new ArrayList<>();
    private final JTabbedPane rounds = new JTabbedPane();
    private final DefaultTableModel scores = new DefaultTableModel(
            new String[]{"Player", "Wins", "Draws", "Losses", "Points"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel pairScores = new DefaultTableModel(
            new String[]{"Player", "Opponent", "0 (LL)", "0.5 (LD)", "1 (WL/DD)",
                    "1.5 (WD)", "2 (WW)", "Complete", "Incomplete"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private static final class GamePair {
        final String player;
        final String opponent;
        final GameController[] legs = new GameController[2];
        GamePair(GameConfiguration configuration) {
            player = configuration.getWhite().getName();
            opponent = configuration.getBlack().getName();
        }
    }
    private record PairLeg(GamePair pair, int leg) {}
    private record Matchup(String player, String opponent) {}
    private final List<GamePair> pairs = new ArrayList<>();
    private final List<GamePair> currentPairs = new ArrayList<>();
    private final Map<GameController, PairLeg> pairLegs = new HashMap<>();
    private final Map<ScheduledGame, PairLeg> replayLegs = new IdentityHashMap<>();

    private final JButton replayTimeouts = new JButton("Replay timeouts");
    private final Map<GameController, ScheduledGame> gameSettings = new LinkedHashMap<>();
    private final Deque<ScheduledGame> replayQueue = new ArrayDeque<>();
    private int parallelLimit = 1;
    private final JButton next = new JButton("Add pairs");
    private final JLabel status = new JLabel("Configure the first round");
    private boolean ended;
    private boolean starting;
    private int roundNumber;

    public TournamentWindow() {
        super("Bot tournament");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                abortTournament();
                dispose();
            }
        });
        JPanel info = new JPanel(new BorderLayout(8, 8));
        info.setPreferredSize(new Dimension(600, 500));
        info.add(status, BorderLayout.NORTH);
        JPanel results = new JPanel(new BorderLayout(8, 8));
        JPanel standingsPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        JScrollPane playerPane = new JScrollPane(new JTable(scores));
        playerPane.setBorder(BorderFactory.createTitledBorder("Individual games"));
        standingsPanel.add(playerPane);
        JTable pairTable = new JTable(pairScores);
        pairTable.setToolTipText("One row per matchup; scores are from the Player column’s perspective.");
        pairTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < pairTable.getColumnCount(); column++)
            pairTable.getColumnModel().getColumn(column).setPreferredWidth(column < 2 ? 110 : 85);
        JScrollPane pairPane = new JScrollPane(pairTable);
        pairPane.setBorder(BorderFactory.createTitledBorder("Pair scores by matchup"));
        standingsPanel.add(pairPane);
        results.add(standingsPanel, BorderLayout.CENTER);
        JTable gameCounts = new JTable(resolutions);
        gameCounts.setFillsViewportHeight(true);
        JScrollPane countsPane = new JScrollPane(gameCounts);
        countsPane.setPreferredSize(new Dimension(350, 190));
        countsPane.setBorder(BorderFactory.createTitledBorder("Games by resolution"));
        results.add(countsPane, BorderLayout.SOUTH);
        info.add(results, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, 1, 6, 6));
        next.addActionListener(e -> configureRound());
        actions.add(next);
        replayTimeouts.setEnabled(false);
        replayTimeouts.addActionListener(e -> replayTimeouts());
        actions.add(replayTimeouts);
        JButton abortRound = new JButton("Abort games in this round");
        abortRound.addActionListener(e -> { roundsRemaining = 0; replayQueue.clear(); abort(currentRound); });
        actions.add(abortRound);
        JButton abortAll = new JButton("Abort tournament");
        abortAll.addActionListener(e -> abortTournament());
        actions.add(abortAll);
        JButton export = new JButton("Export all games");
        export.addActionListener(e -> export(games));
        actions.add(export);
        info.add(actions, BorderLayout.SOUTH);
        add(rounds, BorderLayout.CENTER);
        add(info, BorderLayout.EAST);
        setSize(1450, 850);
        setLocationRelativeTo(null);
    }

    public void configureRound() {
        if (ended || starting || currentRound.stream().anyMatch(g -> g.getState().isRunning())) return;
        JDialog dialog = new JDialog(this, "Configure round " + (roundNumber + 1), true);
        JTabbedPane tabs = new JTabbedPane();
        List<PairingForm> forms = new ArrayList<>();
        JCheckBox randomFen = new JCheckBox("Random FEN from py/fens.txt (seed 1)", true);
        Runnable addGame = () -> {
            PairingForm form = new PairingForm();
            if (!forms.isEmpty()) form.copyFrom(forms.get(forms.size() - 1));
            form.fen.setEnabled(!randomFen.isSelected());
            forms.add(form);
            tabs.addTab("Game " + forms.size(), form);
            tabs.setSelectedComponent(form);
        };
        addGame.run();
        JPanel buttons = new JPanel();
        JSpinner repeat = new JSpinner(new SpinnerNumberModel(5, 1, 5000, 1));
        buttons.add(new JLabel("Pairs per matchup (2 rounds each):"));
        buttons.add(repeat);
        buttons.add(new JLabel("Each opening is played in both colors"));
        randomFen.setToolTipText("Reversed rounds reuse the preceding round's positions.");
        JPanel options = new JPanel(new GridLayout(0, 1));
        JTextField timeControl = new JTextField("X 0.2", 20);
        JPanel timeSettings = new JPanel();
        timeSettings.add(new JLabel("Time control for all games (N / X 1.5 / S 180 0 ms500)"));
        timeSettings.add(timeControl);
        options.add(timeSettings);
        options.add(randomFen);
        randomFen.addActionListener(e -> {
            for (PairingForm form : forms) form.fen.setEnabled(!randomFen.isSelected());
        });
        JButton add = new JButton("Add game");
        add.addActionListener(e -> addGame.run());
        JButton remove = new JButton("Remove game");
        remove.addActionListener(e -> {
            int index = tabs.getSelectedIndex();
            if (forms.size() > 1 && index >= 0) { forms.remove(index); tabs.remove(index); }
        });
        JButton start = new JButton("Start round");
        start.addActionListener(e -> {
            List<GameConfiguration> configurations = new ArrayList<>();
            FenSequence selectedPositions;
            try {
                selectedPositions = randomFen.isSelected()
                        ? new FenSequence(Path.of("py", "fens.txt")) : null;
                // A name identifies a participant across every board and round.
                Map<String, File> identities = new HashMap<>(participants);
                for (PairingForm form : forms) configurations.add(form.configuration(identities, selectedPositions != null, timeControl.getText().trim()));
                participants.clear();
                participants.putAll(identities);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(dialog, error.getMessage(), "Invalid game", JOptionPane.ERROR_MESSAGE);
                return;
            }
            automaticGames = new ArrayList<>();
            for (int i = 0; i < forms.size(); i++) {
                PairingForm form = forms.get(i);
                automaticGames.add(new ScheduledGame(configurations.get(i), form.whiteFile,
                        form.blackFile, timeControl.getText().trim()));
            }
            randomPositions = selectedPositions;
            scheduledRoundIndex = 0;
            roundsRemaining = 2 * (Integer) repeat.getValue() - 1;
            dialog.dispose();
            startRound(configurations);
        });
        buttons.add(add); buttons.add(remove); buttons.add(start);
        dialog.add(tabs);
        options.add(buttons);
        dialog.add(options, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        if (roundNumber == 0 && !starting) dispose();
    }

    private final Map<String, File> participants = new HashMap<>();

    private void startRound(List<GameConfiguration> configurations) {
        startRound(configurations, automaticGames, false);
    }

    private void startRound(List<GameConfiguration> configurations, List<ScheduledGame> templates, boolean replay) {
        if (!replay) parallelLimit = Math.max(parallelLimit, configurations.size());
        if (!replay && randomPositions != null) {
            List<String> positions = randomPositions.nextRound(configurations.size(),
                    scheduledRoundIndex % 2 == 1);
            List<GameConfiguration> selected = new ArrayList<>();
            for (int i = 0; i < configurations.size(); i++) {
                GameConfiguration game = configurations.get(i);
                selected.add(new GameConfiguration(positions.get(i), game.getWhite(), game.getBlack(),
                        automaticGames.get(i).time(), new SwingTicker()));
            }
            configurations = selected;
        }
        starting = true;
        next.setEnabled(false);
        currentRound.clear();
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        rounds.addTab("Round " + (++roundNumber), new JScrollPane(grid));
        rounds.setSelectedIndex(rounds.getTabCount() - 1);
        setVisible(true);
        if (!replay && scheduledRoundIndex % 2 == 0) currentPairs.clear();
        int gameIndex = 0;
        for (GameConfiguration configuration : configurations) {
            GameController controller = new DefaultGameController(new GameManager());
            currentRound.add(controller);
            attemptedGames.add(controller);
            int slot = gameIndex++;
            ScheduledGame template = templates.get(slot);
            PairLeg assignment;
            if (replay) {
                assignment = Objects.requireNonNull(replayLegs.remove(template));
            } else if (scheduledRoundIndex % 2 == 0) {
                GamePair pair = new GamePair(configuration);
                pairs.add(pair);
                currentPairs.add(pair);
                assignment = new PairLeg(pair, 0);
            } else {
                assignment = new PairLeg(currentPairs.get(slot), 1);
            }
            assignment.pair().legs[assignment.leg()] = controller;
            pairLegs.put(controller, assignment);
            boolean reversed = !replay && scheduledRoundIndex % 2 == 1;
            gameSettings.put(controller, new ScheduledGame(configuration,
                    reversed ? template.black() : template.white(),
                    reversed ? template.white() : template.black(), template.time()));
            JPanel cell = new JPanel(new BorderLayout());
            ChessBoardPanel board = new ChessBoardPanel(controller);
            board.setPreferredSize(new Dimension(360, 360));
            cell.add(board);
            JLabel label = new JLabel(configuration.getWhite().getName() + " – " + configuration.getBlack().getName());
            cell.add(label, BorderLayout.NORTH);
            JPanel controls = new JPanel();
            JButton abort = new JButton("Abort");
            abort.setEnabled(false);
            abort.addActionListener(e -> abort(List.of(controller)));
            JButton export = new JButton("Export");
            export.setEnabled(false);
            export.addActionListener(e -> export(List.of(controller)));
            controls.add(abort); controls.add(export);
            cell.add(controls, BorderLayout.SOUTH);
            grid.add(cell);
            controller.addStateListener(state -> SwingUtilities.invokeLater(() -> {
                if (startupErrors.containsKey(controller)) {
                    abort.setEnabled(false);
                    label.setText(configuration.getWhite().getName() + " – "
                            + configuration.getBlack().getName() + ": Error — " + startupErrors.get(controller));
                    refresh();
                    return;
                }
                abort.setEnabled(state.isRunning());
                label.setText(configuration.getWhite().getName() + " – " + configuration.getBlack().getName()
                        + ": " + (state.isRunning() ? "Playing" : state.getTermination())
                        + (state.getErrorMessage() == null ? "" : " (" + state.getErrorMessage() + ")"));
                refresh();
            }));
            // UCI handshakes must not block the Swing event dispatch thread.
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    controller.startGame(configuration);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        games.add(controller);
                        export.setEnabled(true);
                        if (ended || abortRequested.contains(controller)) controller.stopGame();
                    } catch (Exception error) {
                        Throwable cause = error.getCause() == null ? error : error.getCause();
                        String message = cause.getMessage() == null ? cause.toString() : cause.getMessage();
                        startupErrors.put(controller, message);
                        games.remove(controller);
                        export.setEnabled(false);
                        abort.setEnabled(false);
                        try {
                            controller.stopGame();
                        } catch (RuntimeException cleanupError) {
                            message += " (cleanup: " + cleanupError.getMessage() + ")";
                            startupErrors.put(controller, message);
                        } finally {
                            // Players may have started before GameManager installed them.
                            configuration.getWhite().endGame();
                            configuration.getBlack().endGame();
                        }
                        label.setText(configuration.getWhite().getName() + " – "
                                + configuration.getBlack().getName() + ": Error — " + message);
                    } finally {
                        pending--;
                        if (pending == 0) starting = false;
                        refresh();
                    }
                }
            }.execute();
        }
        // done() runs on the EDT after this method returns.
        pending = configurations.size();
        grid.revalidate();
        refresh();
    }

    private FenSequence randomPositions;
    private int scheduledRoundIndex;
    private int roundsRemaining;
    private boolean advancementQueued;
    private List<ScheduledGame> automaticGames = List.of();

    record ScheduledGame(GameConfiguration configuration, File white, File black, String time) {
        GameConfiguration create(boolean reverse) {
            return new GameConfiguration(configuration.getFen(),
                    new EnginePlayer(Sides.WHITE, reverse ? configuration.getBlack().getName() : configuration.getWhite().getName(),
                            reverse ? black : white),
                    new EnginePlayer(Sides.BLACK, reverse ? configuration.getWhite().getName() : configuration.getBlack().getName(),
                            reverse ? white : black),
                    time, new SwingTicker());
        }
    }

    private int pending;
    private final Set<GameController> abortRequested = new HashSet<>();

    private void refresh() {
        scores.setRowCount(0);
        Map<String, double[]> standings = Standings.calculate(
                games.stream().map(GameController::getState).toList());
        // Players whose games never started still appear with zero points.
        participants.keySet().forEach(name -> standings.putIfAbsent(name, new double[4]));
        standings.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[3], a.getValue()[3]))
                .forEach(entry -> {
                    double[] s = entry.getValue();
                    scores.addRow(new Object[]{entry.getKey(), (int)s[0], (int)s[1], (int)s[2], s[3]});
                });
        refreshPairScores();
        resolutions.setRowCount(0);
        String[] categories = {"Starting", "Ongoing", "Finished normally", "Timeout", "Error", "Aborted"};
        for (String category : categories) {
            resolutions.addRow(new Object[]{category,
                    countResolution(currentRound, category), countResolution(attemptedGames, category)});
        }
        resolutions.addRow(new Object[]{"Total", currentRound.size(), attemptedGames.size()});
        long running = currentRound.stream().filter(g -> g.getState().isRunning()).count();
        status.setText((ended ? "Tournament aborted" : "Round " + roundNumber) + " · " + running + " active games · " + (roundsRemaining + (replayQueue.size() + parallelLimit - 1) / parallelLimit) + " rounds queued");
        boolean idle = !starting && running == 0 && roundsRemaining == 0 && replayQueue.isEmpty();
        next.setEnabled(!ended && idle);
        replayTimeouts.setEnabled(idle && !advancementQueued && !timedOutGames().isEmpty());
        if (!ended && !starting && running == 0
                && (roundsRemaining > 0 || !replayQueue.isEmpty()) && !advancementQueued) {
            advancementQueued = true;
            SwingUtilities.invokeLater(() -> {
                advancementQueued = false;
                if (ended) return;
                if (!replayQueue.isEmpty()) {
                    startReplayRound();
                    return;
                }
                if (roundsRemaining == 0) return;
                roundsRemaining--;
                scheduledRoundIndex++;
                boolean reverse = scheduledRoundIndex % 2 == 1;
                startRound(automaticGames.stream().map(game -> game.create(reverse)).toList());
            });
        }
    }

    private GameState pairState(GameController game) {
        return game == null || startupErrors.containsKey(game) ? null : game.getState();
    }

    private void refreshPairScores() {
        Map<Matchup, long[]> counts = new LinkedHashMap<>();
        for (GamePair pair : pairs) {
            int bucket = PairStatistics.bucket(pairState(pair.legs[0]), pairState(pair.legs[1]));
            Matchup matchup = new Matchup(pair.player, pair.opponent);
            Matchup opposite = new Matchup(pair.opponent, pair.player);
            // Keep the first encountered orientation as the row's scoring perspective.
            // A pair configured in the opposite direction still contributes only once.
            if (counts.containsKey(opposite)) {
                matchup = opposite;
                if (bucket >= 0) bucket = 4 - bucket;
            }
            long[] bins = counts.computeIfAbsent(matchup, k -> new long[6]);
            bins[bucket < 0 ? 5 : bucket]++;
        }
        pairScores.setRowCount(0);
        counts.forEach((matchup, bins) -> pairScores.addRow(new Object[]{
                matchup.player(), matchup.opponent(), bins[0], bins[1], bins[2], bins[3], bins[4],
                bins[0] + bins[1] + bins[2] + bins[3] + bins[4], bins[5]}));
    }

    private List<GameController> timedOutGames() {
        return attemptedGames.stream()
                .filter(game -> !startupErrors.containsKey(game)
                        && game.getState().getTermination() == GameTermination.TIMEOUT)
                .toList();
    }

    private void replayTimeouts() {
        if (starting || roundsRemaining != 0 || !replayQueue.isEmpty()
                || currentRound.stream().anyMatch(game -> game.getState().isRunning())) return;
        List<GameController> timedOut = timedOutGames();
        if (timedOut.isEmpty()) return;
        for (GameController game : timedOut) {
            ScheduledGame settings = gameSettings.get(game);
            replayLegs.put(settings, pairLegs.get(game));
            replayQueue.addLast(settings);
        }
        // Superseded attempts stay available on their boards and in PGN exports,
        // but the summary counts only their replacement attempts.
        attemptedGames.removeAll(timedOut);
        currentRound.removeAll(timedOut);
        ended = false;
        startReplayRound();
    }

    private void startReplayRound() {
        List<ScheduledGame> batch = new ArrayList<>();
        while (!replayQueue.isEmpty() && batch.size() < parallelLimit)
            batch.add(replayQueue.removeFirst());
        // create(false) preserves the exact colors and FEN of the timed-out attempt.
        startRound(batch.stream().map(game -> game.create(false)).toList(), batch, true);
    }

    private long countResolution(List<GameController> targets, String category) {
        return targets.stream().filter(game -> resolution(game).equals(category)).count();
    }

    private String resolution(GameController game) {
        if (startupErrors.containsKey(game)) return "Error";
        GameState state = game.getState();
        if (state.isRunning()) return "Ongoing";
        return switch (state.getTermination()) {
            case NONE -> "Starting";
            case CHECKMATE, STALEMATE, DRAW, RESIGNATION -> "Finished normally";
            case TIMEOUT -> "Timeout";
            case ERROR -> "Error";
            case ABORTED -> "Aborted";
        };
    }

    private void abort(List<GameController> targets) {
        abortRequested.addAll(targets);
        for (GameController game : targets) if (game.getState().isRunning()) game.stopGame();
        refresh();
    }

    private void abortTournament() {
        ended = true;
        roundsRemaining = 0;
        replayQueue.clear();
        abort(currentRound);
    }

    private void export(List<GameController> targets) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("tournament.pgn"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Replace " + file.getName() + "?",
                "Export", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            StringBuilder pgn = new StringBuilder();
            for (GameController game : targets) pgn.append(game.getPGN()).append("\n\n");
            Files.writeString(file.toPath(), pgn, StandardCharsets.UTF_8);
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class PairingForm extends JPanel {
        private final JTextField whiteName = new JTextField("White", 20);
        private final JTextField blackName = new JTextField("Black", 20);
        private final JTextField fen = new JTextField("startpos", 30);
        private final JLabel wp = new JLabel("No engine selected");
        private final JLabel bp = new JLabel("No engine selected");
        private File whiteFile;
        private File blackFile;

        PairingForm() {
            super(new GridLayout(0, 2, 8, 8));
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            add(new JLabel("White engine name")); add(whiteName);
            JButton white = new JButton("Choose white executable…");
            white.addActionListener(e -> {
                File file = choose();
                if (file != null) { whiteFile = file; whiteName.setText(file.getName()); wp.setText(file.getPath()); }
            });
            add(white); add(wp);
            add(new JLabel("Black engine name")); add(blackName);
            JButton black = new JButton("Choose black executable…");
            black.addActionListener(e -> {
                File file = choose();
                if (file != null) { blackFile = file; blackName.setText(file.getName()); bp.setText(file.getPath()); }
            });
            add(black); add(bp);
            add(new JLabel("Starting FEN")); add(fen);
            add(new JLabel("Reuse the same name for the same participant."));
            add(new JLabel("Each game starts separate engine processes."));
        }

        private void copyFrom(PairingForm source) {
            whiteName.setText(source.whiteName.getText());
            blackName.setText(source.blackName.getText());
            fen.setText(source.fen.getText());
            whiteFile = source.whiteFile;
            blackFile = source.blackFile;
            wp.setText(source.wp.getText());
            bp.setText(source.bp.getText());
        }

        private File choose() {
            return EngineFileChooser.choose(this);
        }

        GameConfiguration configuration(Map<String, File> identities, boolean randomPosition, String timeControl) throws Exception {
            String w = whiteName.getText().trim(), b = blackName.getText().trim();
            if (w.isEmpty() || b.isEmpty() || w.equals(b))
                throw new IllegalArgumentException("Use distinct, nonempty participant names.");
            register(identities, w, whiteFile);
            register(identities, b, blackFile);
            String position = randomPosition ? "startpos" : fen.getText().trim();
            new hedgineGUI.core.chess.Board(position.isEmpty() || position.equals("startpos")
                    ? GameConfiguration.STANDARD_START_FEN : position);
            hedgineGUI.game.clock.ClockBuilder.setClock(new hedgineGUI.game.clock.Clock(), timeControl);
            return new GameConfiguration(position, new EnginePlayer(Sides.WHITE, w, whiteFile),
                    new EnginePlayer(Sides.BLACK, b, blackFile), timeControl, new SwingTicker());
        }

        private void register(Map<String, File> identities, String name, File file) throws IOException {
            if (file == null || !file.isFile() || !file.canExecute())
                throw new IllegalArgumentException("Choose an executable engine for " + name);
            File canonical = file.getCanonicalFile();
            File previous = identities.putIfAbsent(name, canonical);
            if (previous != null && !previous.equals(canonical))
                throw new IllegalArgumentException("Participant " + name + " already uses a different executable. Use a different name.");
        }
    }
}
