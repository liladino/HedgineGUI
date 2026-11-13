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
}
