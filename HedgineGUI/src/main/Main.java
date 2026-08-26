package main;

import java.io.File;

import javax.swing.SwingUtilities;

import control.DefaultGameController;
import control.GameController;
import game.GameConfiguration;
import game.GameException;
import game.GameManager;
import game.HumanPlayer;
import graphics.MainWindow;
import graphics.SwingTicker;
import utility.Sides;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startApplication);
    }

    private static void startApplication() {
        File savesDirectory = new File(System.getProperty("user.dir"), "saves");
        if (!savesDirectory.exists()) {
            savesDirectory.mkdirs();
        }

        GameController controller = new DefaultGameController(new GameManager());
        new MainWindow(controller);
        try {
            controller.startGame(new GameConfiguration(
                    "startpos",
                    new HumanPlayer(Sides.WHITE, "White"),
                    new HumanPlayer(Sides.BLACK, "Black"),
                    "N",
                    new SwingTicker()));
        } catch (GameException error) {
            throw new IllegalStateException("Default game configuration is invalid", error);
        }
    }
}
