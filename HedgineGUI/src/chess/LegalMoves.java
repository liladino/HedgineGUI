package chess;

import java.util.ArrayList;

public class LegalMoves extends ArrayList<Move> {
	private static final long serialVersionUID = 9706338641357L;

	@SuppressWarnings("unused")
	private LegalMoves() {}
	
	public LegalMoves(Board board) {
		super();
		generateLegalMoves();
	}
	
	private void generateLegalMoves() {
		
	}
}
