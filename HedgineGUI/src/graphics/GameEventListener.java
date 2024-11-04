package graphics;

import utility.Sides;

public interface GameEventListener {
	void onCheckmate(Sides won);
	void onDraw();
	void onStalemate();
	void onInsufficientMaterial();
}
