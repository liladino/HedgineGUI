package hedgineGUI.graphics.tournament;

import java.awt.*;
import javax.swing.*;

import hedgineGUI.control.GameController;
import hedgineGUI.control.GameState;
import hedgineGUI.game.GameConfiguration;
import hedgineGUI.graphics.chessBoard.ChessBoardPanel;

/** A single tournament board with its status line and per-game buttons. */
final class GameCell extends JPanel {
    private final String players;
    private final JLabel label;
    private final JButton abort = new JButton("Abort");
    private final JButton export = new JButton("Export");

    GameCell(GameController controller, GameConfiguration configuration, Runnable onAbort, Runnable onExport) {
        super(new BorderLayout());
        players = configuration.getWhite().getName() + " – " + configuration.getBlack().getName();
        ChessBoardPanel board = new ChessBoardPanel(controller);
        board.setPreferredSize(new Dimension(BoardGrid.MIN_BOARD, BoardGrid.MIN_BOARD));
        add(board);
        label = new JLabel(players);
        add(label, BorderLayout.NORTH);
        JPanel controls = new JPanel();
        abort.setEnabled(false);
        abort.addActionListener(e -> onAbort.run());
        export.setEnabled(false);
        export.addActionListener(e -> onExport.run());
        controls.add(abort); controls.add(export);
        add(controls, BorderLayout.SOUTH);
    }

    void showState(GameState state) {
        abort.setEnabled(state.isRunning());
        label.setText(players + ": " + (state.isRunning() ? "Playing" : state.getTermination())
                + (state.getErrorMessage() == null ? "" : " (" + state.getErrorMessage() + ")"));
    }

    void showStarted() {
        export.setEnabled(true);
    }

    void showError(String message) {
        abort.setEnabled(false);
        export.setEnabled(false);
        label.setText(players + ": Error — " + message);
    }
}
