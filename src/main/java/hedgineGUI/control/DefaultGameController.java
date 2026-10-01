package hedgineGUI.control;

import java.util.concurrent.CopyOnWriteArrayList;

import hedgineGUI.core.IO.PGNConverter;
import hedgineGUI.core.chess.Move;
import hedgineGUI.game.GameConfiguration;
import hedgineGUI.game.GameException;
import hedgineGUI.game.GameManager;
import hedgineGUI.game.Player;

/** Translates GameManager snapshots and commands into the UI-facing API. */
public final class DefaultGameController implements GameController {
    private final GameManager gameManager;
    private final CopyOnWriteArrayList<GameStateListener> listeners =
            new CopyOnWriteArrayList<>();
    private volatile GameState state;
    private GameConfiguration startConfiguration;

    public DefaultGameController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.state = gameManager.getSnapshot();
        gameManager.addStateListener(this::receiveSnapshot);
        startConfiguration = null;
    }

    @Override
    public void startGame(GameConfiguration configuration) throws GameException {
        startConfiguration = configuration;
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
        return gameManager.getBoardCopy().convertToFEN();
    }

    @Override
    public String getStartFEN(){
        return gameManager.getStartFEN();
    }

    @Override
    public String getPGN(){
        return PGNConverter.convertToPGN(
            startConfiguration.getWhite().getName(),
            startConfiguration.getBlack().getName(),
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
