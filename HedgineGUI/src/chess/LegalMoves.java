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
	private boolean enemyPiece(char c) {
		if (c >= 'a' && c <= 'z') {
			if (currentCol == Sides.white) return true;
			else return false;
		}
		else if (c >= 'A' && c <= 'Z'){
			if (currentCol == Sides.black) return true;
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
					case 'q': addQueenMoves(k, l); break;
					case 'k': addKingMoves(k, l); break;
					case 'p': addPawnMoves(k, l); break;
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
		
		for (int j = 0; j <= 1; j++) {
			for (int i = -1; i <= 1; i += 2) {
				int a = 1;
				while (board.boardAt(k + i * j * a, l + i * (1-j) * a) == ' ' || enemyPiece(board.boardAt(k + i * j * a, l + i * (1-j) * a))) {
					if (friendlyPiece(board.boardAt(k + i * j * a, l + i * (1-j) * a))) break;
					
					Move m = new Move(from, new Square(k + i * j * a, l + i * (1-j) * a), ' ');
					if (m.isNull()) break; //probably can't reach this
					//add move
					board.makeMove(m);
					if (!board.inCheck(currentCol)){
						super.add(m);
					}
					//undo move
					board = new Board(copyBoard);
					
					if (enemyPiece(board.boardAt(k + i * j * a, l + i * (1-j) * a))) break;
					
					a++;
				}
			}
		}
	}
	
	private void addBishopMoves(int k, int l) {
		Square from = new Square(k, l);
		
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				int a = 1;
				while (board.boardAt(k + a * j, l + a * i) == ' ' || enemyPiece(board.boardAt(k + a * j, l + a * i))) {
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
					
					if (enemyPiece(board.boardAt(k + a * j, l + a * i))) break;
					
					a++;
				}
			}
		}
	}
	
	private void addQueenMoves(int k, int l) {
		Square from = new Square(k, l);
		
		//bishop dir
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				int a = 1;
				while (board.boardAt(k + a * j, l + a * i) == ' ' || enemyPiece(board.boardAt(k + a * j, l + a * i))) {
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
					
					if (enemyPiece(board.boardAt(k + a * j, l + a * i))) break;
					
					a++;
				}
			}
		}
		
		//rook dir
		for (int j = 0; j <= 1; j++) {
			for (int i = -1; i <= 1; i += 2) {
				int a = 1;
				while (board.boardAt(k + i * j * a, l + i * (1-j) * a) == ' ' || enemyPiece(board.boardAt(k + i * j * a, l + i * (1-j) * a))) {
					if (friendlyPiece(board.boardAt(k + i * j * a, l + i * (1-j) * a))) break;
					
					Move m = new Move(from, new Square(k + i * j * a, l + i * (1-j) * a), ' ');
					if (m.isNull()) break; //probably can't reach this
					//add move
					board.makeMove(m);
					if (!board.inCheck(currentCol)){
						super.add(m);
					}
					//undo move
					board = new Board(copyBoard);
					
					if (enemyPiece(board.boardAt(k + i * j * a, l + i * (1-j) * a))) break;
					
					a++;
				}
			}
		}
	}
	
	private void addKingMoves(int k, int l) {
		Square from = new Square(k, l);
		boolean movingRightWasLegal = false;
		boolean movingLeftWasLegal = false;
		
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) { continue; }
				if (friendlyPiece(board.boardAt(k + j, l + i))) continue;
				if (board.boardAt(k + j, l + i) == 0) continue;
				
				Move m = new Move(from, new Square(k + j, l + i), ' ');
				if (m.isNull()) break; //probably can't reach this

				//add move
				board.makeMove(m);
				if (!board.inCheck(currentCol)){
					super.add(m);
					if (j == 0 && i == 1) movingRightWasLegal = true;
					if (j == 0 && i == -1) movingLeftWasLegal = true;
				}
				//undo move
				board = new Board(copyBoard);
			}
		}
		if (l != 6) return;
		if (board.inCheck(currentCol)) return;
		
		//short castle
		if (currentCol == Sides.white && k == 2 && movingRightWasLegal && board.wKingSideCastling()) {
			Move m = new Move(from, new Square(2, 8), ' ');
			//add move
			board.makeMove(m);
			if (!board.inCheck(currentCol)){
				super.add(m);
			}
			//undo move
			board = new Board(copyBoard);
		}
		if (currentCol == Sides.black && k == 9 && movingRightWasLegal && board.bKingSideCastling()) {
			Move m = new Move(from, new Square(9, 8), ' ');
			//add move
			board.makeMove(m);
			if (!board.inCheck(currentCol)){
				super.add(m);
			}
			//undo move
			board = new Board(copyBoard);
		}
		//long castle
		if (currentCol == Sides.white && k == 2 && movingLeftWasLegal && board.wQueenSideCastling()) {
			Move m = new Move(from, new Square(2, 4), ' ');
			//add move
			board.makeMove(m);
			if (!board.inCheck(currentCol)){
				super.add(m);
			}
			//undo move
			board = new Board(copyBoard);
		}
		if (currentCol == Sides.black && k == 9 && movingLeftWasLegal && board.bQueenSideCastling()) {
			Move m = new Move(from, new Square(9, 4), ' ');
			//add move
			board.makeMove(m);
			if (!board.inCheck(currentCol)){
				super.add(m);
			}
			//undo move
			board = new Board(copyBoard);
		}
	}
	
	private void addPawnMoves(int k, int l) {
		Square from = new Square(k, l);
		int dir = (currentCol == Sides.white ? 1 : -1);
		
		//no take
		if (board.boardAt(k + dir, l) == ' ') {
			if (k + dir != 9 && k + dir != 2) {
				Move m = new Move(from, new Square(k + dir, l), ' ');
				//add move
				board.makeMove(m);
				if (!board.inCheck(currentCol)){
					super.add(m);
				}
				//undo move
				board = new Board(copyBoard);
				//moving 2 squares
				if (((currentCol == Sides.white && k == 3) || (currentCol == Sides.black && k == 8))
					&& board.boardAt(k + 2 * dir, l) == ' '){
					m = new Move(from, new Square(k + 2 * dir, l), ' ');
					//add move
					board.makeMove(m);
					if (!board.inCheck(currentCol)){
						super.add(m);
					}
					//undo move
					board = new Board(copyBoard);
				}
			}
			else {
				char[] prom = {'q', 'r', 'n', 'b'};
				for (int i = 0; i < 4; i++) {
					Move m = new Move(from, new Square(k + dir, l), prom[i]);
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
		
		//taking 
		for (int y = -1; y <= 1; y += 2) {
			if (!board.getEnPassantTarget().isNull() && board.getEnPassantTarget().equals(new Square(k + dir, l + y)) 
				|| enemyPiece(board.boardAt(k + dir, l + y))) {
				if (k + dir != 2 && k + dir != 9) {
					Move m = new Move(from, new Square(k + dir, l + y), ' ');
					//add move
					board.makeMove(m);
					if (!board.inCheck(currentCol)){
						super.add(m);
					}
					//undo move
					board = new Board(copyBoard);
				}
				else {
					char[] prom = {'q', 'r', 'n', 'b'};
					for (int i = 0; i < 4; i++) {
						Move m = new Move(from, new Square(k + dir, l + y), prom[i]);
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
		}
	}
}
