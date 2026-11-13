package hedgineGUI.graphics.tournament;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import hedgineGUI.control.*;
import hedgineGUI.game.*;
import hedgineGUI.graphics.SwingTicker;
import hedgineGUI.tournament.*;

/** Manual pairings, concurrent games per round, and a persistent tournament history. */
public final class TournamentWindow extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(TournamentWindow.class.getName());
    private record PairLeg(GamePair pair, int leg) {}

    // Game attempts
    private final List<GameController> games = new ArrayList<>();
    private final List<GameController> attemptedGames = new ArrayList<>();
    private final List<GameController> currentRound = new ArrayList<>();
    private final Map<GameController, String> startupErrors = new HashMap<>();
    private final Set<GameController> abortRequested = new HashSet<>();
    private final Map<GameController, ScheduledGame> gameSettings = new LinkedHashMap<>();
    private final Map<String, File> participants = new HashMap<>();

    // Pairing of games into reversed-color pairs
    private final List<GamePair> pairs = new ArrayList<>();
    private final List<GamePair> currentPairs = new ArrayList<>();
    private final Map<GameController, PairLeg> pairLegs = new HashMap<>();
    private final Map<ScheduledGame, PairLeg> replayLegs = new IdentityHashMap<>();

    // Round scheduling
    private final Deque<ScheduledGame> replayQueue = new ArrayDeque<>();
    private List<ScheduledGame> automaticGames = List.of();
    private FenSequence randomPositions;
    private int parallelLimit = 1;
    private int scheduledRoundIndex;
    private int roundsRemaining;
    private int roundNumber;
    private int pending;
    private boolean ended;
    private boolean starting;
    private boolean advancementQueued;
    private boolean roundAborted;

    // Results file
    private Path resultsFile;
    private int savedRound;

    private final JTabbedPane rounds = new JTabbedPane();
    private final StandingsPanel standings = new StandingsPanel();
    private final JButton next = new JButton("Add pairs");

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
        info.add(standings, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, 1, 6, 6));
        next.addActionListener(e -> configureRound());
        actions.add(next);
        JButton abortRound = new JButton("Abort games in this round");
        abortRound.addActionListener(e -> {
            roundsRemaining = 0;
            replayQueue.clear();
            roundAborted = true;
            abort(currentRound);
        });
        actions.add(abortRound);
        JButton abortAll = new JButton("Abort tournament");
        abortAll.addActionListener(e -> abortTournament());
        actions.add(abortAll);
        JButton export = new JButton("Export all games");
        export.addActionListener(e -> PgnExport.export(this, games));
        actions.add(export);
        info.add(actions, BorderLayout.SOUTH);
        // The divider lets the user trade board space for the standings panel.
        info.setMinimumSize(new Dimension(250, 0));
        rounds.setMinimumSize(new Dimension(250, 0));
        rounds.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rounds, info);
        split.setResizeWeight(1.0);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);
        setSize(1450, 850);
        setLocationRelativeTo(null);
    }

    public void configureRound() {
        if (ended || starting || currentRound.stream().anyMatch(g -> g.getState().isRunning())) return;
        RoundDialog.RoundSetup setup = RoundDialog.show(this, roundNumber + 1, participants);
        if (setup == null) {
            if (roundNumber == 0) dispose();
            return;
        }
        participants.clear();
        participants.putAll(setup.participants());
        automaticGames = setup.games();
        randomPositions = setup.positions();
        scheduledRoundIndex = 0;
        roundsRemaining = 2 * setup.pairsPerMatchup() - 1;
        startRound(setup.configurations());
    }

    private void startRound(List<GameConfiguration> configurations) {
        startRound(configurations, automaticGames, false);
    }

    private void startRound(List<GameConfiguration> configurations, List<ScheduledGame> templates, boolean replay) {
        if (resultsFile == null) resultsFile = Path.of(System.getProperty("user.dir"), "saves",
                "tournament_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".txt");
        if (!replay) parallelLimit = Math.max(parallelLimit, configurations.size());
        if (!replay && randomPositions != null) configurations = withRandomPositions(configurations);
        starting = true;
        roundAborted = false;
        next.setEnabled(false);
        currentRound.clear();
        BoardGrid grid = new BoardGrid();
        rounds.addTab("Round " + (++roundNumber), new JScrollPane(grid));
        rounds.setSelectedIndex(rounds.getTabCount() - 1);
        setVisible(true);
        if (!replay && scheduledRoundIndex % 2 == 0) currentPairs.clear();
        for (int slot = 0; slot < configurations.size(); slot++) {
            GameConfiguration configuration = configurations.get(slot);
            GameController controller = new DefaultGameController(new GameManager());
            currentRound.add(controller);
            attemptedGames.add(controller);
            ScheduledGame template = templates.get(slot);
            assignPairLeg(controller, configuration, template, slot, replay);
            boolean reversed = !replay && scheduledRoundIndex % 2 == 1;
            gameSettings.put(controller, new ScheduledGame(configuration,
                    reversed ? template.black() : template.white(),
                    reversed ? template.white() : template.black(), template.time()));
            GameCell cell = new GameCell(controller, configuration,
                    () -> abort(List.of(controller)), () -> PgnExport.export(this, List.of(controller)));
            grid.add(cell);
            controller.addStateListener(state -> SwingUtilities.invokeLater(() -> {
                if (startupErrors.containsKey(controller)) cell.showError(startupErrors.get(controller));
                else cell.showState(state);
                refresh();
            }));
            launch(controller, configuration, cell);
        }
        // done() runs on the EDT after this method returns.
        pending = configurations.size();
        grid.revalidate();
        refresh();
    }

    private List<GameConfiguration> withRandomPositions(List<GameConfiguration> configurations) {
        List<String> positions = randomPositions.nextRound(configurations.size(), scheduledRoundIndex % 2 == 1);
        List<GameConfiguration> selected = new ArrayList<>();
        for (int i = 0; i < configurations.size(); i++) {
            GameConfiguration game = configurations.get(i);
            selected.add(new GameConfiguration(positions.get(i), game.getWhite(), game.getBlack(),
                    automaticGames.get(i).time(), new SwingTicker()));
        }
        return selected;
    }

    /** Even scheduled rounds open new pairs, odd ones complete them, replays take over the replaced leg. */
    private void assignPairLeg(GameController controller, GameConfiguration configuration,
            ScheduledGame template, int slot, boolean replay) {
        PairLeg assignment;
        if (replay) {
            assignment = Objects.requireNonNull(replayLegs.remove(template));
        } else if (scheduledRoundIndex % 2 == 0) {
            GamePair pair = new GamePair(configuration.getWhite().getName(), configuration.getBlack().getName());
            pairs.add(pair);
            currentPairs.add(pair);
            assignment = new PairLeg(pair, 0);
        } else {
            assignment = new PairLeg(currentPairs.get(slot), 1);
        }
        assignment.pair().setLeg(assignment.leg(), controller);
        pairLegs.put(controller, assignment);
    }

    private void launch(GameController controller, GameConfiguration configuration, GameCell cell) {
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
                    cell.showStarted();
                    if (ended || abortRequested.contains(controller)) controller.stopGame();
                } catch (Exception error) {
                    Throwable cause = error.getCause() == null ? error : error.getCause();
                    String message = cause.getMessage() == null ? cause.toString() : cause.getMessage();
                    startupErrors.put(controller, message);
                    games.remove(controller);
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
                    cell.showError(message);
                } finally {
                    pending--;
                    if (pending == 0) starting = false;
                    refresh();
                }
            }
        }.execute();
    }

    private void refresh() {
        Map<String, double[]> scores = Standings.calculate(games.stream().map(GameController::getState).toList());
        // Players whose games never started still appear with zero points.
        participants.keySet().forEach(name -> scores.putIfAbsent(name, new double[4]));
        standings.showStandings(scores);
        standings.showMatchups(MatchupScores.calculate(pairs, this::pairState));
        standings.showResolutions(resolutions(currentRound), resolutions(attemptedGames));
        long running = currentRound.stream().filter(g -> g.getState().isRunning()).count();
        List<GameController> timedOut = starting || running > 0 ? List.of() : roundTimeouts();
        int replays = replayQueue.size() + timedOut.size();
        standings.setStatus((ended ? "Tournament aborted" : "Round " + roundNumber) + " · " + running + " active games · "
                + (roundsRemaining + (replays + parallelLimit - 1) / parallelLimit) + " rounds queued");
        if (!starting && running == 0 && roundNumber > savedRound) {
            savedRound = roundNumber;
            saveResults();
        }
        boolean idle = !starting && running == 0 && roundsRemaining == 0 && replays == 0;
        next.setEnabled(!ended && idle);
        if (!ended && !starting && running == 0
                && (roundsRemaining > 0 || replays > 0) && !advancementQueued) {
            advancementQueued = true;
            SwingUtilities.invokeLater(this::advance);
        }
    }

    private void advance() {
        advancementQueued = false;
        if (ended) return;
        // Timeouts are replayed before the next scheduled round.
        queueReplays(roundTimeouts());
        if (!replayQueue.isEmpty()) {
            startReplayRound();
            return;
        }
        if (roundsRemaining == 0) return;
        roundsRemaining--;
        scheduledRoundIndex++;
        boolean reverse = scheduledRoundIndex % 2 == 1;
        startRound(automaticGames.stream().map(game -> game.create(reverse)).toList());
    }

    private List<Resolution> resolutions(List<GameController> targets) {
        return targets.stream()
                .map(game -> Resolution.of(game.getState(), startupErrors.containsKey(game)))
                .toList();
    }

    private GameState pairState(GameController game) {
        return game == null || startupErrors.containsKey(game) ? null : game.getState();
    }

    /** Timed-out games of the current round, unless the user aborted it. */
    private List<GameController> roundTimeouts() {
        if (ended || roundAborted) return List.of();
        return currentRound.stream()
                .filter(game -> !startupErrors.containsKey(game)
                        && game.getState().getTermination() == GameTermination.TIMEOUT)
                .toList();
    }

    private void queueReplays(List<GameController> timedOut) {
        for (GameController game : timedOut) {
            ScheduledGame settings = gameSettings.get(game);
            replayLegs.put(settings, pairLegs.get(game));
            replayQueue.addLast(settings);
        }
        // Superseded attempts stay available on their boards and in PGN exports,
        // but the summary counts only their replacement attempts.
        attemptedGames.removeAll(timedOut);
        currentRound.removeAll(timedOut);
    }

    private void startReplayRound() {
        List<ScheduledGame> batch = new ArrayList<>();
        while (!replayQueue.isEmpty() && batch.size() < parallelLimit)
            batch.add(replayQueue.removeFirst());
        // create(false) preserves the exact colors and FEN of the timed-out attempt.
        startRound(batch.stream().map(game -> game.create(false)).toList(), batch, true);
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

    /** Overwrites the tournament's results file with the current contents of the standings panel. */
    private void saveResults() {
        try {
            Files.createDirectories(resultsFile.getParent());
            Files.writeString(resultsFile, standings.toText(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            LOGGER.log(Level.WARNING, "Could not save tournament results to " + resultsFile, error);
        }
    }
}
