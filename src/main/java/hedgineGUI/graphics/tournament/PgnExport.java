package hedgineGUI.graphics.tournament;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;

import hedgineGUI.control.GameController;

/** Asks for a target file and writes the games into it as one PGN. */
final class PgnExport {
    private PgnExport() {}

    static void export(Component parent, List<GameController> games) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("tournament.pgn"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (file.exists() && JOptionPane.showConfirmDialog(parent, "Replace " + file.getName() + "?",
                "Export", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            StringBuilder pgn = new StringBuilder();
            for (GameController game : games) pgn.append(game.getPGN()).append("\n\n");
            Files.writeString(file.toPath(), pgn, StandardCharsets.UTF_8);
        } catch (IOException error) {
            JOptionPane.showMessageDialog(parent, error.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
