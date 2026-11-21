package hedgineGUI.tournament;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Append one completed round at a time, rolling back a failed append before retrying. */
public final class PgnArchive {
    private final Path path;
    private long committedBytes;

    /** The target must be new; existing archives must never be overwritten. */
    public PgnArchive(Path path) throws IOException {
        this.path = path.toAbsolutePath();
        Files.createDirectories(this.path.getParent());
        Files.createFile(this.path);
    }

    public Path path() { return path; }

    public void append(Iterable<String> games) throws IOException {
        long nextCommittedBytes;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            if (channel.size() < committedBytes)
                throw new IOException("PGN archive was truncated: " + path);
            // Also removes a partial write if the previous rollback itself failed.
            channel.truncate(committedBytes);
            channel.position(committedBytes);
            try {
                for (String pgn : games) {
                    ByteBuffer bytes = StandardCharsets.UTF_8.encode(pgn + "\n\n");
                    while (bytes.hasRemaining()) channel.write(bytes);
                }
                channel.force(true);
                nextCommittedBytes = channel.position();
            } catch (IOException | RuntimeException error) {
                try { channel.truncate(committedBytes); channel.force(true); }
                catch (IOException rollback) { error.addSuppressed(rollback); }
                throw error;
            }
        }
        committedBytes = nextCommittedBytes;
    }
}
