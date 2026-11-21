package hedgineGUI.graphics.tournament;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RoundSetupTest {
    @Test void dataGenerationRunsOneRoundPerRequestedGameEvenWithZeroRandomPlies() {
        var setup = new RoundDialog.RoundSetup(List.of(), List.of(), null, 5, Map.of(), 0, true);
        assertEquals(5, setup.rounds());
    }

    @Test void tournamentStillRunsTwoRoundsPerRequestedPair() {
        var setup = new RoundDialog.RoundSetup(List.of(), List.of(), null, 5, Map.of(), 0, false);
        assertEquals(10, setup.rounds());
    }
}
