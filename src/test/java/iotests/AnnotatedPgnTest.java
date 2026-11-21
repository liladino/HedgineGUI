package iotests;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import hedgineGUI.core.IO.PGNConverter;
import hedgineGUI.core.chess.*;
import hedgineGUI.game.GameConfiguration;
import hedgineGUI.utility.Result;

class AnnotatedPgnTest {
    @Test void exportsCentipawnsAndDepthOnlyForMovesWithMetadata() {
        Move annotated = new Move("e7e5").withEvaluation(new MoveEvaluation(-35, 8));
        List<Move> moves = List.of(new Move("e2e4"), new Move(annotated), new Move("g1f3"));
        String pgn = PGNConverter.convertToPGN("W", "B", GameConfiguration.STANDARD_START_FEN, moves, Result.ONGOING);
        assertTrue(pgn.contains("1. e4 e5 {-35/8} 2. Nf3 *"), pgn);
        assertFalse(PGNConverter.convertToMoves(GameConfiguration.STANDARD_START_FEN, moves).contains("{"));
        assertEquals(new Move("e7e5"), annotated);
        assertEquals(new Move("e7e5").hashCode(), annotated.hashCode());
    }

    @Test void preservesBlackToMoveAndFullMoveNumber() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 5";
        String pgn = PGNConverter.convertToPGN("W", "B", fen,
                List.of(new Move("e7e5").withEvaluation(new MoveEvaluation(-35, 8))), Result.ONGOING);
        assertTrue(pgn.contains("5... e5 {-35/8}"), pgn);
    }
}
