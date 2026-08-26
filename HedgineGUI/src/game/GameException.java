package game;

/** Checked failure while configuring or starting a game. */
public final class GameException extends Exception {
    private static final long serialVersionUID = 1L;

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
