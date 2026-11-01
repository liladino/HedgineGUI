package hedgineGUI.tournament;

import java.util.*;
import hedgineGUI.control.GameState;
import hedgineGUI.game.GameTermination;
import hedgineGUI.utility.Result;

/** Rebuild from snapshots rather than incrementing on repeated state notifications. */
public final class Standings {
    private Standings() {}
    public static Map<String, double[]> calculate(Collection<GameState> games) {
        Map<String, double[]> scores = new TreeMap<>();
        for (GameState game : games) {
            double[] white = scores.computeIfAbsent(game.getWhiteName(), k -> new double[4]);
            double[] black = scores.computeIfAbsent(game.getBlackName(), k -> new double[4]);
            if (game.isRunning() || game.getTermination() == GameTermination.NONE
                    || game.getTermination() == GameTermination.ABORTED
                    || game.getTermination() == GameTermination.ERROR) continue;
            Result result = game.getResult();
            if (result == Result.WHITE_WON) { white[0]++; black[2]++; }
            else if (result == Result.BLACK_WON) { black[0]++; white[2]++; }
            else if (result == Result.DRAW || result == Result.STALEMATE) {
                white[1]++; black[1]++;
            }
            white[3] = white[0] + white[1] / 2;
            black[3] = black[0] + black[1] / 2;
        }
        return scores;
    }
}
