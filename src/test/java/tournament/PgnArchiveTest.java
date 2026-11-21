package tournament;

import hedgineGUI.tournament.PgnArchive;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class PgnArchiveTest {
    @TempDir Path directory;

    @Test void appendsRoundsWithoutReplacingEarlierGames() throws Exception {
        var archive = new PgnArchive(directory.resolve("nested/games.pgn"));
        archive.append(List.of("first {-35/8}", "second"));
        archive.append(List.of()); // A round where no engines started.
        archive.append(List.of("third"));
        assertEquals("first {-35/8}\n\nsecond\n\nthird\n\n", Files.readString(archive.path()));
        assertThrows(FileAlreadyExistsException.class, () -> new PgnArchive(archive.path()));
    }

    @Test void failedRoundIsRolledBackAndRetryDoesNotDuplicateGames() throws Exception {
        var archive = new PgnArchive(directory.resolve("games.pgn"));
        archive.append(List.of("earlier"));
        Iterable<String> failedRound = () -> new Iterator<>() {
            int index;
            public boolean hasNext() { return index < 2; }
            public String next() {
                if (index++ == 0) return "partial";
                throw new IllegalStateException("Cannot serialize game");
            }
        };
        assertThrows(IllegalStateException.class, () -> archive.append(failedRound));
        assertEquals("earlier\n\n", Files.readString(archive.path()));
        archive.append(List.of("partial", "last"));
        assertEquals("earlier\n\npartial\n\nlast\n\n", Files.readString(archive.path()));
    }

    @Test void detectsLostArchivedDataRatherThanSilentlyContinuing() throws Exception {
        var archive = new PgnArchive(directory.resolve("games.pgn"));
        archive.append(List.of("earlier"));
        Files.writeString(archive.path(), "");
        assertThrows(java.io.IOException.class, () -> archive.append(List.of("next")));
    }
}
