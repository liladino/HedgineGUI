package tournament;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import hedgineGUI.core.chess.*;
import hedgineGUI.game.*;
import hedgineGUI.game.clock.HeadlessTicker;
import hedgineGUI.tournament.RandomOpening;
import hedgineGUI.utility.Sides;

class RandomOpeningTest {
    @Test void repeatsLegalUnannotatedMovesWithSameSeed() throws Exception {
        String fen = GameConfiguration.STANDARD_START_FEN;
        List<Move> moves = RandomOpening.generate(fen, 10, new Random(1));
        assertEquals(10, moves.size());
        assertEquals(moves, RandomOpening.generate(fen, 10, new Random(1)));
        Board board = new Board(fen);
        for (Move move : moves) {
            assertTrue(board.isMoveLegal(move));
            assertNull(move.getEvaluation());
            board.makeMove(move);
        }
        assertTrue(RandomOpening.generate(fen, 0, new Random(1)).isEmpty());
        assertTrue(RandomOpening.generate("7k/6Q1/5K2/8/8/8/8/8 b - - 0 1", 20, new Random(1)).isEmpty());
    }

    @Test void gameStartsAfterPrefixAndKeepsFullHistoryWithoutSearchAnnotations() throws Exception {
        String fen = GameConfiguration.STANDARD_START_FEN;
        List<Move> prefix = RandomOpening.generate(fen, 5, new Random(1));
        GameManager manager = new GameManager();
        manager.startGame(new GameConfiguration(fen, new HumanPlayer(Sides.WHITE, "W"),
                new HumanPlayer(Sides.BLACK, "B"), "N", new HeadlessTicker(), 0, prefix));
        try {
            Board expected = new Board(fen);
            prefix.forEach(expected::makeMove);
            assertEquals(expected.convertToFEN(), manager.getBoardCopy().convertToFEN());
            assertEquals(prefix, manager.getMoves());
            assertEquals(fen, manager.getStartFEN());
            assertFalse(manager.takeBack()); // The prescribed opening is not player input.
        } finally {
            manager.stopGame();
        }
    }
}
