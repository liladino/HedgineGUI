package hedgineGUI.tournament;

import hedgineGUI.control.GameState;
import hedgineGUI.utility.Result;

/** Scores two valid games from the first game's White player's perspective. */
public final class PairStatistics {
    private PairStatistics() {}

    /** Bucket 0..4 represents 0, 0.5, 1, 1.5, 2 points; -1 means incomplete. */
    public static int bucket(GameState first, GameState reversed) {
        if (!valid(first) || !valid(reversed)) return -1;
        if (!first.getWhiteName().equals(reversed.getBlackName())
                || !first.getBlackName().equals(reversed.getWhiteName())
                || !first.getInitialFen().equals(reversed.getInitialFen())) return -1;
        return whiteHalfPoints(first.getResult()) + (2 - whiteHalfPoints(reversed.getResult()));
    }

    private static boolean valid(GameState state) {
        if (state == null || state.isRunning()) return false;
        return switch (state.getTermination()) {
            case CHECKMATE, STALEMATE, DRAW, RESIGNATION ->
                state.getResult() != Result.ONGOING;
            default -> false;
        };
    }

    private static int whiteHalfPoints(Result result) {
        return switch (result) {
            case WHITE_WON -> 2;
            case BLACK_WON -> 0;
            case DRAW, STALEMATE -> 1;
            case ONGOING -> throw new IllegalArgumentException("Unfinished game");
        };
    }
}
