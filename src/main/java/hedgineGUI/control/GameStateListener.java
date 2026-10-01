package hedgineGUI.control;

@FunctionalInterface
public interface GameStateListener {
    void onGameStateChanged(GameState state);
}
