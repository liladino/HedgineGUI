package hedgineGUI.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import java.io.File;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import hedgineGUI.control.DefaultGameController;
import hedgineGUI.core.chess.Move;
import hedgineGUI.game.clock.HeadlessTicker;
import hedgineGUI.utility.Sides;

class EngineAnnotationIntegrationTest {
    @TempDir Path directory;

    private File engine(String mode) throws Exception {
        assumeFalse(System.getProperty("os.name").startsWith("Windows"));
        Path script = directory.resolve("engine-" + mode);
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Files.writeString(script, "#!/bin/sh\nexec " + quote(java) + " -cp " + quote(classpath)
                + " hedgineGUI.game.FakeUciEngine " + quote(mode) + " "
                + quote(directory.resolve("protocol.txt").toString()) + "\n");
        assertTrue(script.toFile().setExecutable(true));
        return script.toFile();
    }

    private String quote(String value) { return "'" + value.replace("'", "'\\''") + "'"; }

    private void await(BooleanSupplier condition) {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            while (!condition.getAsBoolean()) Thread.sleep(10);
        });
    }

    private void annotatedGame(int depth) throws Exception {
        var controller = new DefaultGameController(new GameManager());
        try {
            controller.startGame(new GameConfiguration(GameConfiguration.STANDARD_START_FEN,
                    new HumanPlayer(Sides.WHITE, "Human"), new EnginePlayer(Sides.BLACK, "Engine", engine("play"), depth),
                    "X 30", new HeadlessTicker(), depth, List.of(new Move("e2e4"))));
            await(() -> controller.getState().isMoveInputAllowed());
            assertTrue(controller.getPGN().contains("e4 e5 {-35/8}"), controller.getPGN());
            assertTrue(controller.submitMove(new Move("g1f3")));
            await(() -> controller.getState().getMoveHistory().size() == 4);
            String pgn = controller.getPGN();
            assertEquals(1, pgn.chars().filter(c -> c == '{').count(), pgn);
            assertTrue(pgn.contains("Nf3 Nc6"), pgn);
            String protocol = Files.readString(directory.resolve("protocol.txt"));
            assertTrue(protocol.contains("position startpos moves e2e4"), protocol);
            assertTrue(protocol.contains(depth > 0 ? "go depth 8" : "go movetime "), protocol);
        } finally {
            controller.stopGame();
        }
    }

    @Test void depthSearchAnnotatesAndResetsBetweenSearches() throws Exception { annotatedGame(8); }
    @Test void ordinaryTimedPlayAlsoExportsAnnotations() throws Exception { annotatedGame(0); }

    @Test void depthSearchTimeoutUsesTournamentTermination() throws Exception {
        var ticker = new HeadlessTicker();
        var controller = new DefaultGameController(new GameManager());
        try {
            controller.startGame(new GameConfiguration(GameConfiguration.STANDARD_START_FEN,
                    new EnginePlayer(Sides.WHITE, "Engine", engine("stall"), 8),
                    new HumanPlayer(Sides.BLACK, "Other"), "X ms100", ticker, 8, List.of()));
            Thread.sleep(200);
            ticker.simulateTick();
            assertEquals(GameTermination.TIMEOUT, controller.getState().getTermination());
            assertFalse(controller.getState().isRunning());
            assertFalse(controller.getPGN().contains("{"));
        } finally {
            controller.stopGame();
        }
    }
}
