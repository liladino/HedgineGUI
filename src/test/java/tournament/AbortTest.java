package tournament;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import hedgineGUI.control.DefaultGameController;
import hedgineGUI.game.*;
import hedgineGUI.graphics.SwingTicker;
import hedgineGUI.utility.*;

class AbortTest {
    @Test void abortExportsUnfinishedResultAndPreservesCompletedResult() throws Exception {
        var controller = new DefaultGameController(new GameManager());
        controller.startGame(new GameConfiguration("startpos",
                new HumanPlayer(Sides.WHITE, "White"), new HumanPlayer(Sides.BLACK, "Black"),
                "N", new SwingTicker()));
        controller.stopGame();
        assertEquals(GameTermination.ABORTED, controller.getState().getTermination());
        assertEquals(Result.ONGOING, controller.getState().getResult());
        assertTrue(controller.getPGN().contains("[Result \"*\"]"));
        controller.startGame(new GameConfiguration("startpos",
                new HumanPlayer(Sides.WHITE, "White"), new HumanPlayer(Sides.BLACK, "Black"),
                "N", new SwingTicker()));
        controller.resign();
        Result completed = controller.getState().getResult();
        controller.stopGame();
        assertEquals(completed, controller.getState().getResult());
    }
}
