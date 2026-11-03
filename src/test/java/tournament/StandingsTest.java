package tournament;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import hedgineGUI.control.GameState;
import hedgineGUI.core.chess.Board;
import hedgineGUI.game.*;
import hedgineGUI.tournament.Standings;
import hedgineGUI.utility.*;

class StandingsTest {
    private GameState game(String white, String black, Result result, GameTermination termination) throws Exception {
        return new GameState(new Board(GameConfiguration.STANDARD_START_FEN), List.of(), List.of(),
                GameConfiguration.STANDARD_START_FEN, white, black, result, termination, null,
                termination == GameTermination.NONE, false, null, null);
    }

    @Test void countsSameParticipantAcrossSimultaneousGamesAndColors() throws Exception {
        var games = List.of(game("new", "old", Result.WHITE_WON, GameTermination.CHECKMATE),
                game("old", "new", Result.DRAW, GameTermination.DRAW),
                game("new", "old", Result.ONGOING, GameTermination.NONE));
        var scores = Standings.calculate(games);
        assertArrayEquals(new double[]{1, 1, 0, 1.5}, scores.get("new"));
        assertArrayEquals(new double[]{0, 1, 1, 0.5}, scores.get("old"));
        assertArrayEquals(scores.get("new"), Standings.calculate(games).get("new"));
    }

    @Test void excludesAbortsAndErrorsEvenWithDecisiveResult() throws Exception {
        var scores = Standings.calculate(List.of(
                game("new", "old", Result.WHITE_WON, GameTermination.ABORTED),
                game("new", "old", Result.BLACK_WON, GameTermination.ERROR)));
        assertArrayEquals(new double[4], scores.get("new"));
        assertArrayEquals(new double[4], scores.get("old"));
    }

    @Test void timeoutsAwardNeitherPlayerPointsOrWins() throws Exception {
        var scores = Standings.calculate(List.of(
                game("new", "old", Result.WHITE_WON, GameTermination.TIMEOUT),
                game("old", "new", Result.BLACK_WON, GameTermination.TIMEOUT),
                game("new", "old", Result.DRAW, GameTermination.TIMEOUT)));
        assertArrayEquals(new double[4], scores.get("new"));
        assertArrayEquals(new double[4], scores.get("old"));
    }
}
