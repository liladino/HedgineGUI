package hedgineGUI.graphics.dialogs;

import java.awt.Component;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/** Shared engine picker with a per-user directory persisted across launches. */
public final class EngineFileChooser {
    private static final Logger LOGGER = Logger.getLogger(EngineFileChooser.class.getName());
    private static final String LAST_DIRECTORY = "lastEngineDirectory";
    private static File lastDirectory;

    private EngineFileChooser() {}

    public static File choose(Component parent) {
        if (lastDirectory == null) {
            try {
                String saved = preferences().get(LAST_DIRECTORY, "");
                if (!saved.isEmpty()) lastDirectory = new File(saved);
            } catch (SecurityException error) {
                LOGGER.warning("Cannot read engine directory preference: " + error.getMessage());
            }
        }
        JFileChooser chooser = lastDirectory != null && lastDirectory.isDirectory()
                ? new JFileChooser(lastDirectory) : new JFileChooser();
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
        File selected = chooser.getSelectedFile().getAbsoluteFile();
        lastDirectory = selected.getParentFile();
        if (lastDirectory != null) {
            try {
                Preferences prefs = preferences();
                prefs.put(LAST_DIRECTORY, lastDirectory.getPath());
                prefs.flush();
            } catch (SecurityException | BackingStoreException error) {
                LOGGER.warning("Cannot persist engine directory: " + error.getMessage());
            }
        }
        return selected;
    }

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(EngineFileChooser.class);
    }
}
