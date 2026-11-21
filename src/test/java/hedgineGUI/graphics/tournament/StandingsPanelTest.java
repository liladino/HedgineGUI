package hedgineGUI.graphics.tournament;

import hedgineGUI.tournament.Resolution;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StandingsPanelTest {
    @Test void combinesArchivedAndCurrentResolutionCounts() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var panel = new StandingsPanel();
            panel.showResolutions(List.of(Resolution.ONGOING),
                    List.of(Resolution.FINISHED, Resolution.ONGOING),
                    Map.of(Resolution.FINISHED, 1_000_000L, Resolution.ERROR, 3L));
            String text = panel.toText();
            assertTrue(text.contains("Finished normally: 0 1000001"));
            assertTrue(text.contains("Ongoing: 1 1"));
            assertTrue(text.contains("Error: 0 3"));
            assertTrue(text.contains("Total: 1 1000005"));
        });
    }
}
