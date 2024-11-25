package game.interfaces;

import utility.Sides;

/**
 * GameManager notifies thru these functions that the game ended
 */
public interface GameEventListener {
	void onCheckmate(Sides won);
	void onDraw();
	void onStalemate();
	void onInsufficientMaterial();
	void onTimeIsUp(Sides won);
	void onTimeIsUp();
	void onResign(Sides won);
}
