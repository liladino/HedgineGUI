package hedgineGUI.tournament;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import hedgineGUI.core.chess.Board;
import hedgineGUI.core.IO.FENException;

/** Deterministic draws in board order, independent of engine completion order. */
public final class FenSequence {
    public static final long SEED = 1L;
    private final List<String> positions;
    private final Random random = new Random(SEED);
    private List<String> previousRound = List.of();

    public FenSequence(Path file) throws IOException {
        List<String> loaded = new ArrayList<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String fen = lines.get(i).trim();
            if (fen.isEmpty()) continue;
            try {
                new Board(fen);
            } catch (FENException | RuntimeException error) {
                throw new IOException("Invalid FEN in " + file + " at line " + (i + 1)
                        + ": " + error.getMessage(), error);
            }
            loaded.add(fen);
        }
        if (loaded.isEmpty()) throw new IOException("No FEN positions in " + file);
        positions = List.copyOf(loaded);
    }

    /** Reversed rounds reuse board-for-board positions without consuming random draws. */
    public List<String> nextRound(int games, boolean reusePrevious) {
        if (games < 1) throw new IllegalArgumentException("A round needs at least one game");
        if (reusePrevious) {
            if (previousRound.size() != games)
                throw new IllegalArgumentException("Reversed round must match the previous round");
            return previousRound;
        }
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < games; i++) selected.add(positions.get(random.nextInt(positions.size())));
        previousRound = List.copyOf(selected);
        return previousRound;
    }
}
