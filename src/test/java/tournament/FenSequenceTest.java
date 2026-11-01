package tournament;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import hedgineGUI.tournament.FenSequence;

class FenSequenceTest {
    @TempDir Path directory;
    private static final List<String> FENS = List.of(
            "8/8/8/8/8/8/4k3/7K w - - 0 1",
            "8/8/8/8/8/8/4k3/7K b - - 0 1",
            "8/8/8/8/8/8/4k3/7K w - - 0 2");

    private Path source() throws IOException {
        Path file = directory.resolve("fens.txt");
        Files.write(file, FENS);
        return file;
    }

    @Test void sameSeedReproducesDrawsInGameOrderAcrossRounds() throws IOException {
        Path file = source();
        FenSequence first = new FenSequence(file), second = new FenSequence(file);
        Random expected = new Random(FenSequence.SEED);
        for (int round = 0; round < 4; round++) {
            List<String> positions = first.nextRound(5, false);
            assertEquals(positions, second.nextRound(5, false));
            for (String fen : positions) assertEquals(FENS.get(expected.nextInt(FENS.size())), fen);
        }
    }

    @Test void reversedRoundsReusePositionsWithoutAdvancingRandomSequence() throws IOException {
        Path file = source();
        FenSequence paired = new FenSequence(file), unpaired = new FenSequence(file);
        List<String> first = paired.nextRound(4, false);
        assertEquals(unpaired.nextRound(4, false), first);
        assertEquals(first, paired.nextRound(4, true));
        assertEquals(unpaired.nextRound(4, false), paired.nextRound(4, false));
        assertThrows(IllegalArgumentException.class, () -> paired.nextRound(3, true));
    }

    @Test void reportsMissingEmptyAndInvalidFiles() throws IOException {
        assertThrows(IOException.class, () -> new FenSequence(directory.resolve("missing")));
        Path file = directory.resolve("bad.txt");
        Files.writeString(file, "\n  \n");
        assertThrows(IOException.class, () -> new FenSequence(file));
        Files.writeString(file, FENS.get(0) + "\ninvalid fen\n");
        IOException error = assertThrows(IOException.class, () -> new FenSequence(file));
        assertTrue(error.getMessage().contains("line 2"));
    }
}
