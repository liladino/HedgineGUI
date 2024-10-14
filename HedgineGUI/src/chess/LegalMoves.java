package chess;

import java.util.ArrayList;

public class LegalMoves extends ArrayList<Move> {
	private static final long serialVersionUID = 9706338641357L;
	
	private Board board;
	private Board copyBoard;
	
	Sides currentCol;
	
	@SuppressWarnings("unused")
	private LegalMoves() {}
	
	public LegalMoves(Board board) {
		super();
		this.board = new Board(board);
		copyBoard = new Board(board);
		currentCol = board.tomove();
		generateLegalMoves();
	}
	
	private boolean friendlyPiece(char c) {
		if (c >= 'a' && c <= 'z') {
			if (currentCol == Sides.black) return true;
			else return false;
		}
		else if (c >= 'A' && c <= 'Z'){
			if (currentCol == Sides.white) return true;
			else return false;
		}
		return false;
	}
	
	private void generateLegalMoves() {
		int colorOffset = (board.tomove() == Sides.black ? 0 : 'a' - 'A');
		for (int k = 2; k < 10; k++) {
			for (int l = 2; l < 10; l++) {
				switch(board.boardAt(k, l) + colorOffset) {
					case 'n': addKnightMoves(k, l); break;
					case 'r': addRookMoves(k, l); break;
					case 'b': addBishopMoves(k, l); break;
				}
			}
		}
	}
	
	private void addKnightMoves(int k, int l) {
		Square from = new Square(k, l);
		
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				if (friendlyPiece(board.boardAt(k + i, l + 2 * j))) continue;
				
				Move m = new Move(from, new Square(k + i, l + 2 * j), ' ');
				if (m.isNull()) continue;
				//add move
				board.makeMove(m);
				if (!board.inCheck(currentCol)){
					super.add(m);
				}
				//undo move
				board = new Board(copyBoard);
			}
		}
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				if (friendlyPiece(board.boardAt(k + 2 * i, l + j))) continue;
				
				Move m = new Move(from, new Square(k + 2 * i, l + j), ' ');
				if (m.isNull()) continue;
				//add move
				board.makeMove(m);
				if (!board.inCheck(currentCol)){
					super.add(m);
				}
				//undo move
				board = new Board(copyBoard);
			}
		}
	}
	
	private void addRookMoves(int k, int l) {
		Square from = new Square(k, l);
		
		for (int i = -1; i <= 1; i += 2) {
			int j = 1;
			while (board.boardAt(k + i * j, l) == ' ' || !friendlyPiece(board.boardAt(k + i * j, l))) {
				if (friendlyPiece(board.boardAt(k + i * j, l))) break;
				
				Move m = new Move(from, new Square(k + i * j, l), ' ');
				if (m.isNull()) break; //probably can't reach this
				//add move
				board.makeMove(m);
				if (!board.inCheck(currentCol)){
					super.add(m);
				}
				//undo move
				board = new Board(copyBoard);
				
				j++;
			}
		}
		
		for (int i = -1; i <= 1; i += 2) {
			int j = 1;
			while (board.boardAt(k, i * j + l) == ' ' || !friendlyPiece(board.boardAt(k, i * j + l))) {
				if (friendlyPiece(board.boardAt(k, i * j + l))) break;
				
				Move m = new Move(from, new Square(k, i * j + l), ' ');
				if (m.isNull()) break; //probably can't reach this
				//add move
				board.makeMove(m);
				if (!board.inCheck(currentCol)){
					super.add(m);
				}
				//undo move
				board = new Board(copyBoard);
				
				j++;
			}
		}
	}
	
	private void addBishopMoves(int k, int l) {
		Square from = new Square(k, l);
		

		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				int a = 1;
				while (board.boardAt(k + a * j, l + a * i) == ' ' || !friendlyPiece(board.boardAt(k + a * j, l + a * i))) {
					if (friendlyPiece(board.boardAt(k + a * j, l + a * i))) break;
					
					Move m = new Move(from, new Square(k + a * j, l + a * i), ' ');
					if (m.isNull()) break; //probably can't reach this
					//add move
					board.makeMove(m);
					if (!board.inCheck(currentCol)){
						super.add(m);
					}
					//undo move
					board = new Board(copyBoard);
					
					a++;
				}
			}
		}
	}
}
