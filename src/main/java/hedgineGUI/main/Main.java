package hedgineGUI.main;

import java.io.File;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import hedgineGUI.control.DefaultGameController;
import hedgineGUI.control.GameController;
import hedgineGUI.game.GameConfiguration;
import hedgineGUI.game.GameException;
import hedgineGUI.game.GameManager;
import hedgineGUI.game.HumanPlayer;
import hedgineGUI.graphics.SwingTicker;
import hedgineGUI.graphics.MainWindow;
import hedgineGUI.utility.Sides;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        FlatLightLaf.setup();
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
