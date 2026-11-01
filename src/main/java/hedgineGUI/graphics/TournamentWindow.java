package hedgineGUI.graphics;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import hedgineGUI.control.*;
import hedgineGUI.game.*;
import hedgineGUI.graphics.chessBoard.ChessBoardPanel;
import hedgineGUI.tournament.Standings;
import hedgineGUI.utility.Sides;

/** Manual pairings, concurrent games per round, and a persistent tournament history. */
public final class TournamentWindow extends JFrame {
    private final List<GameController> games = new ArrayList<>();
    private final List<GameController> currentRound = new ArrayList<>();
    private final JTabbedPane rounds = new JTabbedPane();
    private final DefaultTableModel scores = new DefaultTableModel(
            new String[]{"Player", "Wins", "Draws", "Losses", "Points"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JButton next = new JButton("Next round");
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
        info.setPreferredSize(new Dimension(350, 500));
        info.add(status, BorderLayout.NORTH);
        info.add(new JScrollPane(new JTable(scores)), BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, 1, 6, 6));
        next.addActionListener(e -> configureRound());
        actions.add(next);
        JButton abortRound = new JButton("Abort games in this round");
        abortRound.addActionListener(e -> abort(currentRound));
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
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    public void configureRound() {
        if (ended || starting || currentRound.stream().anyMatch(g -> g.getState().isRunning())) return;
        JDialog dialog = new JDialog(this, "Configure round " + (roundNumber + 1), true);
        JTabbedPane tabs = new JTabbedPane();
        List<PairingForm> forms = new ArrayList<>();
        Runnable addGame = () -> {
            PairingForm form = new PairingForm();
            forms.add(form);
            tabs.addTab("Game " + forms.size(), form);
            tabs.setSelectedComponent(form);
        };
        addGame.run();
        JPanel buttons = new JPanel();
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
            try {
                // A name identifies a participant across every board and round.
                Map<String, File> identities = new HashMap<>(participants);
                for (PairingForm form : forms) configurations.add(form.configuration(identities));
                participants.clear();
                participants.putAll(identities);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(dialog, error.getMessage(), "Invalid game", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            startRound(configurations);
        });
        buttons.add(add); buttons.add(remove); buttons.add(start);
        dialog.add(tabs);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        if (roundNumber == 0 && !starting) dispose();
    }

    private final Map<String, File> participants = new HashMap<>();

    private void startRound(List<GameConfiguration> configurations) {
        starting = true;
        next.setEnabled(false);
        currentRound.clear();
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        rounds.addTab("Round " + (++roundNumber), new JScrollPane(grid));
        rounds.setSelectedIndex(rounds.getTabCount() - 1);
        setVisible(true);
        for (GameConfiguration configuration : configurations) {
            GameController controller = new DefaultGameController(new GameManager());
            currentRound.add(controller);
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
                        controller.stopGame();
                        label.setText("Failed: " + error.getMessage());
                    }
                    pending--;
                    if (pending == 0) starting = false;
                    refresh();
                }
            }.execute();
        }
        // done() runs on the EDT after this method returns.
        pending = configurations.size();
        grid.revalidate();
    }

    private int pending;
    private final Set<GameController> abortRequested = new HashSet<>();

    private void refresh() {
        scores.setRowCount(0);
        Standings.calculate(games.stream().map(GameController::getState).toList())
                .entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[3], a.getValue()[3]))
                .forEach(entry -> {
                    double[] s = entry.getValue();
                    scores.addRow(new Object[]{entry.getKey(), (int)s[0], (int)s[1], (int)s[2], s[3]});
                });
        long running = currentRound.stream().filter(g -> g.getState().isRunning()).count();
        status.setText((ended ? "Tournament aborted" : "Round " + roundNumber) + " · " + running + " active games");
        next.setEnabled(!ended && !starting && running == 0);
    }

    private void abort(List<GameController> targets) {
        abortRequested.addAll(targets);
        for (GameController game : targets) if (game.getState().isRunning()) game.stopGame();
        refresh();
    }

    private void abortTournament() {
        ended = true;
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
        private final JTextField time = new JTextField("X 2", 20);
        private File whiteFile;
        private File blackFile;

        PairingForm() {
            super(new GridLayout(0, 2, 8, 8));
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            add(new JLabel("White engine name")); add(whiteName);
            JButton white = new JButton("Choose white executable…");
            JLabel wp = new JLabel("No engine selected");
            white.addActionListener(e -> {
                File file = choose();
                if (file != null) { whiteFile = file; whiteName.setText(file.getName()); wp.setText(file.getPath()); }
            });
            add(white); add(wp);
            add(new JLabel("Black engine name")); add(blackName);
            JButton black = new JButton("Choose black executable…");
            JLabel bp = new JLabel("No engine selected");
            black.addActionListener(e -> {
                File file = choose();
                if (file != null) { blackFile = file; blackName.setText(file.getName()); bp.setText(file.getPath()); }
            });
            add(black); add(bp);
            add(new JLabel("Starting FEN")); add(fen);
            add(new JLabel("Time control (N / X 60 / S 180 0 2)")); add(time);
            add(new JLabel("Reuse the same name for the same participant."));
            add(new JLabel("Each game starts separate engine processes."));
        }

        private File choose() {
            JFileChooser chooser = new JFileChooser();
            return chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
        }

        GameConfiguration configuration(Map<String, File> identities) throws Exception {
            String w = whiteName.getText().trim(), b = blackName.getText().trim();
            if (w.isEmpty() || b.isEmpty() || w.equals(b))
                throw new IllegalArgumentException("Use distinct, nonempty participant names.");
            register(identities, w, whiteFile);
            register(identities, b, blackFile);
            String position = fen.getText().trim();
            new hedgineGUI.core.chess.Board(position.isEmpty() || position.equals("startpos")
                    ? GameConfiguration.STANDARD_START_FEN : position);
            hedgineGUI.game.clock.ClockBuilder.setClock(new hedgineGUI.game.clock.Clock(), time.getText().trim());
            return new GameConfiguration(position, new EnginePlayer(Sides.WHITE, w, whiteFile),
                    new EnginePlayer(Sides.BLACK, b, blackFile), time.getText().trim(), new SwingTicker());
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
