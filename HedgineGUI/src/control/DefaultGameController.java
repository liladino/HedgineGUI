package control;

import java.util.concurrent.CopyOnWriteArrayList;

import core.IO.PGNConverter;
import core.chess.Move;
import game.GameConfiguration;
import game.GameException;
import game.GameManager;
import game.Player;

/** Translates GameManager snapshots and commands into the UI-facing API. */
public final class DefaultGameController implements GameController {
    private final GameManager gameManager;
    private final CopyOnWriteArrayList<GameStateListener> listeners =
            new CopyOnWriteArrayList<>();
    private volatile GameState state;
    private GameConfiguration startconfConfiguration;

    public DefaultGameController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.state = gameManager.getSnapshot();
        gameManager.addStateListener(this::receiveSnapshot);
        startconfConfiguration = null;
    }

    @Override
    public void startGame(GameConfiguration configuration) throws GameException {
        startconfConfiguration = configuration;
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
    public void smartTakeBack(){
        gameManager.smartTakeBack();
    }

    @Override
    public void stopGame() {
        gameManager.stopGame();
    }

    @Override 
    public void requestMove(){
        gameManager.requestCurrentMove();
    }

    @Override
    public void quitEngines(){
        gameManager.quitEngines();
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override 
    public Player getCurrentPlayer(){
        return gameManager.getCurrentPlayer();
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

    @Override
    public String getFEN(){
        return gameManager.getBoard().convertToFEN();
    }

    @Override
    public String getStartFEN(){
        return gameManager.getStartFEN();
    }

    @Override
    public String getPGN(){
        return PGNConverter.convertToPGN(
            startconfConfiguration.getWhite().getName(), 
            startconfConfiguration.getBlack().getName(), 
            getFEN(), 
            gameManager.getMoves(), 
            gameManager.getResult());
    }

    private void receiveSnapshot(GameState newState) 
    {
        state = newState;
        for (GameStateListener listener : listeners) {
            listener.onGameStateChanged(newState);
        }
    }
}
