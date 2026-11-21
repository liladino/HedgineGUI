package hedgineGUI.graphics.tournament;

import hedgineGUI.tournament.MatchupScores.Matchup;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BatchStatisticsTest {
    @Test void repeatedRefreshesDoNotMutateArchivedScores() {
        Map<String, double[]> archived = Map.of("engine", new double[]{10, 4, 3, 12});
        for (int refresh = 0; refresh < 3; refresh++) {
            Map<String, double[]> live = new HashMap<>();
            live.put("engine", new double[]{1, 2, 0, 2});
            TournamentWindow.mergeScores(live, archived);
            assertArrayEquals(new double[]{11, 6, 3, 14}, live.get("engine"));
        }
        assertArrayEquals(new double[]{10, 4, 3, 12}, archived.get("engine"));
    }

    @Test void pairsFromEarlierTournamentStayInOneOrientationAfterArchiving() {
        var forward = new Matchup("A", "B");
        var reverse = new Matchup("B", "A");
        Map<Matchup, long[]> archived = new LinkedHashMap<>();
        TournamentWindow.mergeMatchups(archived, Map.of(forward, new long[]{1, 2, 3, 4, 5, 6}));
        TournamentWindow.mergeMatchups(archived, Map.of(reverse, new long[]{10, 20, 30, 40, 50, 60}));
        assertEquals(1, archived.size());
        assertArrayEquals(new long[]{51, 42, 33, 24, 15, 66}, archived.get(forward));
        Map<Matchup, long[]> refreshed = new LinkedHashMap<>();
        TournamentWindow.mergeMatchups(refreshed, archived);
        refreshed.get(forward)[0]++;
        assertEquals(51, archived.get(forward)[0]);
    }
}
