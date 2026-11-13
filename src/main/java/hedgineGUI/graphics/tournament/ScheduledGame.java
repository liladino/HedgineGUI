package hedgineGUI.graphics.tournament;

import java.io.File;
import hedgineGUI.game.*;
import hedgineGUI.graphics.SwingTicker;
import hedgineGUI.utility.Sides;

/** Everything needed to start a fresh attempt of a game, optionally with the colors swapped. */
public record ScheduledGame(GameConfiguration configuration, File white, File black, String time) {
    public GameConfiguration create(boolean reverse) {
        return new GameConfiguration(configuration.getFen(),
                new EnginePlayer(Sides.WHITE, reverse ? configuration.getBlack().getName() : configuration.getWhite().getName(),
                        reverse ? black : white),
                new EnginePlayer(Sides.BLACK, reverse ? configuration.getWhite().getName() : configuration.getBlack().getName(),
                        reverse ? white : black),
                time, new SwingTicker());
    }
}
