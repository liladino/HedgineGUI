package control;

import java.util.concurrent.CopyOnWriteArrayList;

import core.chess.Move;
import game.GameConfiguration;
import game.GameException;
import game.GameManager;
import game.GameSnapshot;

/** Translates GameManager snapshots and commands into the UI-facing API. */
public final class DefaultGameController implements GameController {
    private final GameManager gameManager;
    private final CopyOnWriteArrayList<GameStateListener> listeners =
            new CopyOnWriteArrayList<>();
    private volatile GameState state;

    public DefaultGameController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.state = new GameState(gameManager.getSnapshot());
        gameManager.addStateListener(this::receiveSnapshot);
    }

    @Override
    public void startGame(GameConfiguration configuration) throws GameException {
        gameManager.startGame(configuration);
    }

    @Override
    public boolean submitMove(Move move) {
        return gameManager.submitMove(move);
    }

    @Override
    public void resign() {
        gameManager.resign();
    }

    @Override
    public boolean takeBack() {
        return gameManager.takeBack();
    }

    @Override
    public void stopGame() {
        gameManager.stopGame();
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public void addStateListener(GameStateListener listener) {
        listeners.add(listener);
        listener.onGameStateChanged(state);
    }

    @Override
    public void removeStateListener(GameStateListener listener) {
        listeners.remove(listener);
    }

    private void receiveSnapshot(GameSnapshot snapshot) {
        GameState newState = new GameState(snapshot);
        state = newState;
        for (GameStateListener listener : listeners) {
            listener.onGameStateChanged(newState);
        }
    }
}
