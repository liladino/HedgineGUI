package hedgineGUI.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import hedgineGUI.core.IO.FENException;
import hedgineGUI.core.chess.Board;
import hedgineGUI.core.chess.Move;
import hedgineGUI.utility.Result;

/** Uniformly chooses a legal move at each ply, stopping if the game is already over. */
public final class RandomOpening {
    private RandomOpening() {}

    public static List<Move> generate(String fen, int plies, Random random) throws FENException {
        if (plies < 0) throw new IllegalArgumentException("Random plies cannot be negative");
        Board board = new Board(fen);
        List<Move> moves = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        seen.put(positionKey(board), 1);
        for (int i = 0; i < plies && board.getResult() == Result.ONGOING; i++) {
            int count = board.numberOfLegalMoves();
            if (count == 0) break;
            Move move = new Move(board.getLegalMove(random.nextInt(count)));
            moves.add(move);
            board.makeMove(move);
            if (seen.merge(positionKey(board), 1, Integer::sum) >= 3) break;
        }
        return List.copyOf(moves);
    }

    private static String positionKey(Board board) {
        return board.convertToFEN().replaceFirst("\\s+\\S+\\s+\\S+$", "");
    }
}
