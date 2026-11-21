package hedgineGUI.graphics.tournament;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import javax.swing.*;

import hedgineGUI.control.GameController;

/** Streams archived and live games into one PGN without loading the archive into memory. */
final class PgnExport {
    private PgnExport() {}

    static void export(Component parent, List<GameController> games) {
        export(parent, games, null);
    }

    static void export(Component parent, List<GameController> games, Path archive) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("tournament.pgn"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        Path target = chooser.getSelectedFile().toPath().toAbsolutePath();
        if (Files.exists(target) && JOptionPane.showConfirmDialog(parent, "Replace " + target.getFileName() + "?",
                "Export", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Path temporary = null;
        try {
            if (archive != null && (target.equals(archive.toAbsolutePath())
                    || (Files.exists(target) && Files.isSameFile(target, archive))))
                throw new IOException("Choose a different file from the automatic PGN archive.");
            temporary = Files.createTempFile(target.getParent(), "pgn-export-", ".tmp");
            try (var output = Files.newOutputStream(temporary)) {
                if (archive != null) Files.copy(archive, output);
                for (GameController game : games)
                    output.write((game.getPGN() + "\n\n").getBytes(StandardCharsets.UTF_8));
            }
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            JOptionPane.showMessageDialog(parent, error.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) {}
        }
    }
}
