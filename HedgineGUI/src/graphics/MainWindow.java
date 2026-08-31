package graphics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import control.GameController;
import control.GameState;
import game.GameTermination;
import graphics.dialogs.GameEndDialogs;
import graphics.dialogs.InformationDialogs;
import graphics.panels.ChessBoardPanel;
import graphics.panels.RightPanel;

/** Top-level Swing composition root for the UI layer. */
public final class MainWindow extends JFrame {
    private static final long serialVersionUID = 38435486L;

    private final GameController controller;
    private final ChessBoardPanel chessBoardPanel;
    private final RightPanel rightPanel;
    private final MenuManager menuManager;
    private GameTermination lastShownTermination = GameTermination.NONE;

    public MainWindow(GameController controller) {
        this.controller = controller;

        setTitle("Chess");
        setMinimumSize(new Dimension(
                600 + getInsets().left + getInsets().right,
                400 + getInsets().top + getInsets().bottom));
        setSize(
                900 + getInsets().left + getInsets().right,
                640 + getInsets().top + getInsets().bottom);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                MainWindow.this.controller.stopGame();
                dispose();
            }
        });

        GraphicSettings.initializeGraphicSettings();
        menuManager = new MenuManager(this, controller);
        chessBoardPanel = new ChessBoardPanel(controller, menuManager);
        rightPanel = new RightPanel(controller);

        setLayout(new BorderLayout());
        add(chessBoardPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        pack();

        controller.addStateListener(this::receiveState);
        setVisible(true);
    }

    public RightPanel getRightPanel() {
        return rightPanel;
    }

    private void receiveState(GameState state) {
        Runnable renderEndState = () -> {
            GameTermination termination = state.getTermination();
            if (termination == GameTermination.NONE) {
                lastShownTermination = GameTermination.NONE;
                return;
            }
            if (termination == lastShownTermination) {
                return;
            }
            lastShownTermination = termination;

            switch (termination) {
                case CHECKMATE:
                    GameEndDialogs.showCheckmate(this, state.getWinner());
                    break;
                case STALEMATE:
                    GameEndDialogs.showStalemate(this);
                    break;
                case DRAW:
                    GameEndDialogs.showDraw(this);
                    break;
                case RESIGNATION:
                    GameEndDialogs.showResigned(this, state.getWinner());
                    break;
                case TIMEOUT:
                    if (state.getWinner() == null) {
                        GameEndDialogs.showDraw(this);
                    } else {
                        GameEndDialogs.showWonOnTime(this, state.getWinner());
                    }
                    break;
                case ERROR:
                    InformationDialogs.errorDialog(this, state.getErrorMessage());
                    break;
                case ABORTED:
                case NONE:
                    break;
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            renderEndState.run();
        } else {
            SwingUtilities.invokeLater(renderEndState);
        }
    }
}
