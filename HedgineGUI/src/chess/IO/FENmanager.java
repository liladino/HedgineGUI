package chess.IO;

import chess.Board;
import chess.Sides;

public class FENmanager {
	public FENmanager(){ }
	
	public String convertToFEN(Board board) {
		StringBuilder sb = new StringBuilder();
		for (int i = 9; i > 1; i--) {
			int empty = 0;
			for (int j = 2; j < 10; j++) {
				if (board.boardAt(i, j) == ' ') {
					empty++;
				}
				else {
					if (empty > 0) {
						sb.append(empty);
						empty = 0;
					}
					sb.append(board.boardAt(i, j));
				}
			}
			if (empty > 0) {
				sb.append(empty);
			}
			if (i != 2) sb.append('/');
		}
		
		sb.append(' ');
		
		if (board.tomove() == Sides.white) sb.append("w ");
		else sb.append("b ");
		
		if (board.wKingSideCastling()) sb.append('K');
		if (board.wQueenSideCastling()) sb.append('Q');
		if (board.bKingSideCastling()) sb.append('k');
		if (board.bQueenSideCastling()) sb.append('q');
		
		sb.append(' ');
		
		if (board.enPassantTarget().isNull()) sb.append('-');
		else sb.append(board.enPassantTarget().toString());
		
		sb.append(' ');
		sb.append(board.getFiftyMoveRule());
		
		sb.append(' ');
		sb.append(board.getFullMoveCount());
		
		return new String(sb);
	}
}
