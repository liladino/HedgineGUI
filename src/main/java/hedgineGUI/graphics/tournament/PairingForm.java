package hedgineGUI.graphics.tournament;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.*;

import hedgineGUI.game.*;
import hedgineGUI.graphics.SwingTicker;
import hedgineGUI.graphics.dialogs.EngineFileChooser;
import hedgineGUI.utility.Sides;

/** One game of a round: the two engines and the starting position. */
final class PairingForm extends JPanel {
    private final JTextField whiteName = new JTextField("White", 20);
    private final JTextField blackName = new JTextField("Black", 20);
    private final JTextField fen = new JTextField("startpos", 30);
    private final JLabel wp = new JLabel("No engine selected");
    private final JLabel bp = new JLabel("No engine selected");
    private File whiteFile;
    private File blackFile;

    PairingForm() {
        super(new GridLayout(0, 2, 8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(new JLabel("White engine name")); add(whiteName);
        JButton white = new JButton("Choose white executable…");
        white.addActionListener(e -> {
            File file = EngineFileChooser.choose(this);
            if (file != null) { whiteFile = file; whiteName.setText(file.getName()); wp.setText(file.getPath()); }
        });
        add(white); add(wp);
        add(new JLabel("Black engine name")); add(blackName);
        JButton black = new JButton("Choose black executable…");
        black.addActionListener(e -> {
            File file = EngineFileChooser.choose(this);
            if (file != null) { blackFile = file; blackName.setText(file.getName()); bp.setText(file.getPath()); }
        });
        add(black); add(bp);
        add(new JLabel("Starting FEN")); add(fen);
        add(new JLabel("Reuse the same name for the same participant."));
        add(new JLabel("Each game starts separate engine processes."));
    }

    void copyFrom(PairingForm source) {
        whiteName.setText(source.whiteName.getText());
        blackName.setText(source.blackName.getText());
        fen.setText(source.fen.getText());
        whiteFile = source.whiteFile;
        blackFile = source.blackFile;
        wp.setText(source.wp.getText());
        bp.setText(source.bp.getText());
    }

    void setFenEnabled(boolean enabled) { fen.setEnabled(enabled); }
    File whiteFile() { return whiteFile; }
    File blackFile() { return blackFile; }

    GameConfiguration configuration(Map<String, File> identities, boolean randomPosition, String timeControl) throws Exception {
        String w = whiteName.getText().trim(), b = blackName.getText().trim();
        if (w.isEmpty() || b.isEmpty() || w.equals(b))
            throw new IllegalArgumentException("Use distinct, nonempty participant names.");
        register(identities, w, whiteFile);
        register(identities, b, blackFile);
        String position = randomPosition ? "startpos" : fen.getText().trim();
        new hedgineGUI.core.chess.Board(position.isEmpty() || position.equals("startpos")
                ? GameConfiguration.STANDARD_START_FEN : position);
        hedgineGUI.game.clock.ClockBuilder.setClock(new hedgineGUI.game.clock.Clock(), timeControl);
        return new GameConfiguration(position, new EnginePlayer(Sides.WHITE, w, whiteFile),
                new EnginePlayer(Sides.BLACK, b, blackFile), timeControl, new SwingTicker());
    }

    private void register(Map<String, File> identities, String name, File file) throws IOException {
        if (file == null || !file.isFile() || !file.canExecute())
            throw new IllegalArgumentException("Choose an executable engine for " + name);
        File canonical = file.getCanonicalFile();
        File previous = identities.putIfAbsent(name, canonical);
        if (previous != null && !previous.equals(canonical))
            throw new IllegalArgumentException("Participant " + name + " already uses a different executable. Use a different name.");
    }
}
