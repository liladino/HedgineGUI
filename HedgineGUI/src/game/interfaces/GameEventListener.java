package game.interfaces;

import utility.Sides;

public interface GameEventListener {
	void onCheckmate(Sides won);
	void onDraw();
	void onStalemate();
	void onInsufficientMaterial();
	void onTimeIsUp(Sides won);
	void onTimeIsUp();
}
