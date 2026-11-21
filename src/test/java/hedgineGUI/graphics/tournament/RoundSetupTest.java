package hedgineGUI.graphics.tournament;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RoundSetupTest {
    @Test void dataGenerationRunsOneRoundPerRequestedGameEvenWithZeroRandomPlies() {
        var setup = new RoundDialog.RoundSetup(List.of(), List.of(), null, 5, Map.of(), 0, true, 1000);
        assertEquals(5, setup.rounds());
    }

    @Test void millionGameRunDoesNotExpandTheSchedule() {
        var setup = new RoundDialog.RoundSetup(List.of(), List.of(), null, 1_000_000, Map.of(), 4, true, 1000);
        assertEquals(1_000_000, setup.rounds());
        assertEquals(1000, setup.batchRounds());
        assertTrue(setup.games().isEmpty());
    }

    @Test void tournamentStillRunsTwoRoundsPerRequestedPair() {
        var setup = new RoundDialog.RoundSetup(List.of(), List.of(), null, 5, Map.of(), 0, false, 1000);
        assertEquals(10, setup.rounds());
    }
}
