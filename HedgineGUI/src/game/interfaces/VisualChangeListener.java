package game.interfaces;

/**
 * If the board gets rotated, or the color scheme is changed, the menu signals this
 * */
public interface VisualChangeListener {
	void onGameStateChanged();
	void onGameLooksChanged();
}
