package tournament;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import hedgineGUI.control.GameState;
import hedgineGUI.core.chess.Board;
import hedgineGUI.game.*;
import hedgineGUI.tournament.PairStatistics;
import hedgineGUI.utility.*;

class PairStatisticsTest {
    private GameState state(boolean reversed, Result result, GameTermination termination) throws Exception {
        String fen = GameConfiguration.STANDARD_START_FEN;
        return new GameState(new Board(fen), List.of(), List.of(), fen,
                reversed ? "baseline" : "fix", reversed ? "fix" : "baseline",
                result, termination, null, false, false, null, null);
    }

    @Test void allNineResultsMapToFiveBucketsWithCorrectColorPerspective() throws Exception {
        Result[] first = {Result.BLACK_WON, Result.DRAW, Result.WHITE_WON};
        Result[] second = {Result.WHITE_WON, Result.DRAW, Result.BLACK_WON};
        for (int a = 0; a < 3; a++) for (int b = 0; b < 3; b++)
            assertEquals(a + b, PairStatistics.bucket(
                    state(false, first[a], GameTermination.DRAW),
                    state(true, second[b], GameTermination.DRAW)));
    }

    @Test void incompletePairsBecomeScorableAfterTimeoutReplacement() throws Exception {
        GameState win = state(false, Result.WHITE_WON, GameTermination.CHECKMATE);
        assertEquals(-1, PairStatistics.bucket(win, null));
        for (GameTermination termination : List.of(GameTermination.TIMEOUT, GameTermination.ERROR,
                GameTermination.ABORTED, GameTermination.NONE))
            assertEquals(-1, PairStatistics.bucket(win, state(true, Result.BLACK_WON, termination)));
        assertEquals(4, PairStatistics.bucket(win, state(true, Result.BLACK_WON, GameTermination.CHECKMATE)));
        assertEquals(-1, PairStatistics.bucket(win, win));
    }
}
