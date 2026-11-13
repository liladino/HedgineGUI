package hedgineGUI.graphics;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import hedgineGUI.graphics.tournament.TournamentWindow;

/** Entry point shared by play and tournament windows. */
public final class StartWindow extends JFrame {
    private static final Logger ENGINE_PLAYER_LOGGER = Logger.getLogger("hedgineGUI.game.EnginePlayer");
    
    public StartWindow(Runnable play) {
        super("HedgineGUI");
        GraphicSettings.initializeGraphicSettings();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel choices = new JPanel(new GridLayout(2, 1, 12, 12));
        choices.setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));
        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> play.run());
        JButton tournament = new JButton("Bot tournament");
        tournament.addActionListener(e -> {
            TournamentWindow window = new TournamentWindow();
            window.configureRound();
            ENGINE_PLAYER_LOGGER.setLevel(Level.WARNING);
        });
        choices.add(playButton);
        choices.add(tournament);
        add(choices);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
