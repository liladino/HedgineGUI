package hedgineGUI.game;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import hedgineGUI.core.chess.MoveEvaluation;
import hedgineGUI.utility.Sides;

class UciSearchInfoTest {
    @Test void deepestScoreUsesWhitePerspectiveAndSupportsBothFormats() {
        UciSearchInfo info = new UciSearchInfo();
        info.accept("info depth 8 cp 35", Sides.BLACK);
        info.accept("info depth 4 score cp 999", Sides.BLACK);
        info.accept("info depth 12 multipv 2 score cp 888 pv e7e5", Sides.BLACK);
        assertEquals(new MoveEvaluation(-35, 8), info.finish());
        info.accept("info depth 9 score cp -12 nodes 100 pv e2e4", Sides.WHITE);
        assertEquals(new MoveEvaluation(-12, 9), info.finish());
    }

    @Test void incompleteMalformedAndBoundScoresAreNotInventedOrReused() {
        UciSearchInfo info = new UciSearchInfo();
        info.accept("info depth 8 cp 35", Sides.WHITE);
        info.reset();
        for (String line : new String[]{"info depth 9", "info cp 42", "info depth x cp 4",
                "info depth 7 cp", "info string depth 10 cp 55", "info depth -1 cp 3"})
            info.accept(line, Sides.WHITE);
        assertNull(info.finish());
        info.accept("info depth 7 score cp 5", Sides.WHITE);
        info.accept("info depth 8 score mate 3", Sides.WHITE);
        assertNull(info.finish());
        info.accept("info depth 8 score cp 5 lowerbound", Sides.WHITE);
        assertNull(info.finish());
        info.accept("info depth 8 score cp 5", Sides.WHITE);
        assertNotNull(info.finish());
        assertNull(info.finish());
    }

    @Test void depthSearchDoesNotIncludeTimeLimits() {
        EnginePlayer engine = new EnginePlayer(Sides.WHITE, "test", null, 8);
        Position position = new Position(GameConfiguration.STANDARD_START_FEN,
                GameConfiguration.STANDARD_START_FEN, Sides.WHITE, java.util.List.of(), null);
        assertEquals("go depth 8", engine.buildGoCommand(position));
        assertEquals("go movetime 2000", new EnginePlayer(Sides.WHITE, "test", null).buildGoCommand(position));
    }
}
