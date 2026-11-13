package hedgineGUI.tournament;

import java.util.*;
import java.util.function.Function;
import hedgineGUI.control.GameController;
import hedgineGUI.control.GameState;

/** Pentanomial pair scores, one entry per matchup regardless of which side was configured first. */
public final class MatchupScores {
    public record Matchup(String player, String opponent) {}

    private MatchupScores() {}

    /**
     * Bins 0..4 count pairs worth 0, 0.5, 1, 1.5 and 2 points for the matchup's player; bin 5 counts
     * incomplete pairs. The state function maps a leg (possibly null) to a scorable state or null.
     */
    public static Map<Matchup, long[]> calculate(List<GamePair> pairs, Function<GameController, GameState> state) {
        Map<Matchup, long[]> counts = new LinkedHashMap<>();
        for (GamePair pair : pairs) {
            int bucket = PairStatistics.bucket(state.apply(pair.leg(0)), state.apply(pair.leg(1)));
            Matchup matchup = new Matchup(pair.player(), pair.opponent());
            Matchup opposite = new Matchup(pair.opponent(), pair.player());
            // Keep the first encountered orientation as the row's scoring perspective.
            // A pair configured in the opposite direction still contributes only once.
            if (counts.containsKey(opposite)) {
                matchup = opposite;
                if (bucket >= 0) bucket = 4 - bucket;
            }
            long[] bins = counts.computeIfAbsent(matchup, k -> new long[6]);
            bins[bucket < 0 ? 5 : bucket]++;
        }
        return counts;
    }
}
