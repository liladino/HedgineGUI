package hedgineGUI.tournament;

import hedgineGUI.control.GameState;

/** How a tournament game attempt ended, as summarized in the results panel. */
public enum Resolution {
    STARTING("Starting"),
    ONGOING("Ongoing"),
    FINISHED("Finished normally"),
    TIMEOUT("Timeout"),
    ERROR("Error"),
    ABORTED("Aborted");

    private final String label;

    Resolution(String label) { this.label = label; }

    @Override public String toString() { return label; }

    public static Resolution of(GameState state, boolean failedToStart) {
        if (failedToStart) return ERROR;
        if (state.isRunning()) return ONGOING;
        return switch (state.getTermination()) {
            case NONE -> STARTING;
            case CHECKMATE, STALEMATE, DRAW, RESIGNATION -> FINISHED;
            case TIMEOUT -> TIMEOUT;
            case ERROR -> ERROR;
            case ABORTED -> ABORTED;
        };
    }
}
