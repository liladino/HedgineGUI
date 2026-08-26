package game;

import game.clock.HeadlessTicker;

/**
 * Temporary compatibility helper for older tests and callers.
 * New UI code should compose GameManager and GameController directly.
 */
@Deprecated
public final class GameStarter {
    private final GameManager gameManager = new GameManager();

    public void startNewUIGame(Player white, Player black, String timeControl) {
        startNewUIGame(GameConfiguration.STANDARD_START_FEN, white, black, timeControl);
    }

    public void startNewUIGame(
            String fen,
            Player white,
            Player black,
            String timeControl) {
        try {
            gameManager.startGame(new GameConfiguration(
                    fen, white, black, timeControl, new HeadlessTicker()));
        } catch (GameException error) {
            throw new IllegalArgumentException("Could not start game", error);
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
