package hedgineGUI.graphics.tournament;

import java.io.File;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import hedgineGUI.game.*;
import hedgineGUI.graphics.SwingTicker;
import hedgineGUI.utility.Sides;

class ScheduledGameTest {
    @Test void reversedPairingPreservesPositionAndRestoresOriginalColors() {
        String fen = "8/8/8/8/8/8/4k3/7K b - - 0 1";
        var original = new GameConfiguration(fen,
                new EnginePlayer(Sides.WHITE, "new", new File("new")),
                new EnginePlayer(Sides.BLACK, "old", new File("old")), "X ms500", new SwingTicker());
        var template = new ScheduledGame(original, new File("new"), new File("old"), "X ms500");
        var reversed = template.create(true);
        assertEquals("old", reversed.getWhite().getName());
        assertEquals("new", reversed.getBlack().getName());
        assertEquals(Sides.WHITE, reversed.getWhite().getSide());
        assertEquals(Sides.BLACK, reversed.getBlack().getSide());
        assertEquals(fen, reversed.getFen());
        var normal = template.create(false);
        assertEquals("new", normal.getWhite().getName());
        assertEquals("old", normal.getBlack().getName());
        assertEquals(fen, normal.getFen());
        assertNotSame(original.getWhite(), normal.getWhite());
        assertNotSame(reversed.getWhite(), normal.getBlack());
    }

    @Test void depthAndRandomOpeningSurviveColorSwapAndReplay() {
        var prefix = java.util.List.of(new hedgineGUI.core.chess.Move("e2e4"));
        var config = new GameConfiguration(GameConfiguration.STANDARD_START_FEN,
                new EnginePlayer(Sides.WHITE, "new", new File("new"), 8),
                new EnginePlayer(Sides.BLACK, "old", new File("old"), 8),
                "X 10", new SwingTicker(), 8, prefix);
        var template = new ScheduledGame(config, new File("new"), new File("old"), "X 10");
        var reverse = template.create(true);
        var retry = new ScheduledGame(reverse, new File("old"), new File("new"), "X 10").create(false);
        assertEquals(8, reverse.getSearchDepth());
        assertEquals(8, retry.getSearchDepth());
        assertEquals(prefix, reverse.getInitialMoves());
        assertEquals(prefix, retry.getInitialMoves());
        assertEquals(config.getFen(), retry.getFen());
        assertEquals("old", retry.getWhite().getName());
    }
}
