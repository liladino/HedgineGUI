package chess.IO;

import chess.Board;
import chess.Sides;
import chess.Square;

public class FENmanager {
	boolean[] readSuccesses;
	public FENmanager(){ 
		readSuccesses = new boolean[6];
	}
	
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
		
		if (!board.wKingSideCastling() && !board.wQueenSideCastling() && !board.bKingSideCastling() && !board.bQueenSideCastling()) sb.append('-');
		
		sb.append(' ');
		
		if (board.getEnPassantTarget().isNull()) sb.append('-');
		else sb.append(board.getEnPassantTarget().toString());
		
		sb.append(' ');
		sb.append(board.getFiftyMoveRule());
		
		sb.append(' ');
		sb.append(board.getFullMoveCount());
		
		return new String(sb);
	}
	
	public char[][] startpos() {
		char[][] board = {
				{ 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0, 0 },
				{ 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0, 0 },
				{ 0, 0,'R','N','B','Q','K','B','N','R', 0, 0 },
				{ 0, 0,'P','P','P','P','P','P','P','P', 0, 0 },
				{ 0, 0,' ',' ',' ',' ',' ',' ',' ',' ', 0, 0 },
				{ 0, 0,' ',' ',' ',' ',' ',' ',' ',' ', 0, 0 },
				{ 0, 0,' ',' ',' ',' ',' ',' ',' ',' ', 0, 0 },
				{ 0, 0,' ',' ',' ',' ',' ',' ',' ',' ', 0, 0 },
				{ 0, 0,'p','p','p','p','p','p','p','p', 0, 0 },
				{ 0, 0,'r','n','b','q','k','b','n','b', 0, 0 },
				{ 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0, 0 },
				{ 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0, 0 }
			};
		return board;
	}
	
	public char[][] parseBoard(String FEN) throws FENException{
		char[][] board = startpos();
		
		String[] s = FEN.split(" ");
		
		if (s[0].length() < 14) throw new FENException("Missing element(s)", readSuccesses);
		
		String[] pos = s[0].split("/");

		if (pos.length != 8) throw new FENException("Missing element(s)", readSuccesses);
		
		int wking = 0, bking = 0;
		int k = 0;
		for (int i = 2; i < 10; i++) {
		//	System.out.println(pos[i-2]);
			
			for (int j = 0; j < pos[i-2].length(); j++){
				switch (pos[i-2].charAt(j)) {
					case 'p': board[7 - k / 8 + 2][k % 8 + 2] = 'p'; break;
					case 'r': board[7 - k / 8 + 2][k % 8 + 2] = 'r'; break;
					case 'n': board[7 - k / 8 + 2][k % 8 + 2] = 'n'; break;
					case 'b': board[7 - k / 8 + 2][k % 8 + 2] = 'b'; break;
					case 'q': board[7 - k / 8 + 2][k % 8 + 2] = 'q'; break;
					
					case 'P': board[7 - k / 8 + 2][k % 8 + 2] = 'P'; break;
					case 'R': board[7 - k / 8 + 2][k % 8 + 2] = 'R'; break;
					case 'N': board[7 - k / 8 + 2][k % 8 + 2] = 'N'; break;
					case 'B': board[7 - k / 8 + 2][k % 8 + 2] = 'B'; break;
					case 'Q': board[7 - k / 8 + 2][k % 8 + 2] = 'Q'; break;
					
					case 'K': 
						board[7 - k / 8 + 2][k % 8 + 2] = 'K';
						wking++;
						break;
					case 'k': 
						board[7 - k / 8 + 2][k % 8 + 2] = 'k';
						bking++;
						break;
					
					default:
						if (pos[i-2].charAt(j) < '9' && '0' < pos[i-2].charAt(j)) {
							int temp = pos[i-2].charAt(j) - '0';
							if (k % 8 + temp > 8) {
								throw new FENException("Too long row", 0);
							}
							for (int l = 0; l < temp; l++) {
								board[7 - k / 8 + 2][k % 8 + 2 + l] = ' '; 
							}
							k += temp - 1;
						}
						else {
							throw new FENException("Unexpected character in position: " + pos[i-2].charAt(j), 0);
						}
				}
				k++;
			}
		}
		if (wking != 1 || bking != 1) {
			throw new FENException("Too many/few king, white: " + wking + " black: " + bking, 0);
		}
		

		readSuccesses[0] = true;
		
		return board;
	}
	
	public Sides parseTomove(String FEN) throws FENException{
		String[] s = FEN.split(" ");
		
		if (s.length < 2) {
			throw new FENException("Missing element(s)", readSuccesses);
		}
		
		if (s[1].length() != 1) {
			throw new FENException("Can't parse color", readSuccesses);
		}
		
		switch(s[1].charAt(0)) {
			case 'w': 
				readSuccesses[1] = true;
				return Sides.white;
			case 'b': 
				readSuccesses[1] = true;
				return Sides.black;
		}

		readSuccesses[1] = true;
		throw new FENException("Can't parse color", readSuccesses);
	}
	
	public boolean[] parseCastlingRights(String FEN) throws FENException{
		String[] s = FEN.split(" ");
		
		if (s.length < 3) {
			throw new FENException("Missing element(s)", readSuccesses);
		}
		
		if (s[2].length() > 4) {
			throw new FENException("Can't parse castling rights", readSuccesses);
		}
		
		boolean[] castlingRights = {false, false, false, false};
		
		if (s[2].length() == 1 && s[2].charAt(0) == '-') {
			readSuccesses[2] = true;
			return castlingRights;
		}
		
		for (int i = 0; i < s[2].length(); i++) {
			switch (s[2].charAt(i)) {
				case 'K':
					castlingRights[0] = true;
					break;
				case 'Q':
					castlingRights[1] = true;
					break;
				case 'k':
					castlingRights[2] = true;
					break;
				case 'q':
					castlingRights[3] = true;
					break;
				default:
					throw new FENException("Can't parse castling rights, character found: " + s[2].charAt(i), readSuccesses);
			}
		}

		readSuccesses[2] = true;
		return castlingRights;
	}
	
	public Square parseEnPassant(String FEN) throws FENException {
		String[] s = FEN.split(" ");
		
		if (s.length < 4) {
			throw new FENException("Missing element(s)", readSuccesses);
		}
		
		if (s[3].length() < 1) {
			throw new FENException("Can't parse en passant", readSuccesses);
		}
		if (s[3].length() == 1) {
			if (s[3].charAt(0) == '-') {
				readSuccesses[3] = true;
				return new Square();
			}
			else {
				throw new FENException("Unexpected character in en passant: " + s[3].charAt(0), readSuccesses);
			}
		}
		
		if (s[3].length() != 2) {
			throw new FENException("Unexpected character in en passant: " + s[3], readSuccesses);
		}

		Square s1 = new Square(s[3].charAt(0), s[3].charAt(1) - '0');
		
		if (s1.isNull()) throw new FENException("En passant square is not valid: " + s[3], readSuccesses);

		readSuccesses[3] = true;
		return s1;
	}
	
	public int parseFiftyMoveRule(String FEN) throws FENException {
		String[] s = FEN.split(" ");
		
		if (s.length < 5) {
			throw new FENException("Missing element(s)", readSuccesses);
		}
		
		int num;
		try {
		    num = Integer.parseInt(s[4]);
		} catch (NumberFormatException e) {
				throw new FENException("Invalid number format while parsing fifty move rule: " + e.getMessage(), readSuccesses);
		}

		readSuccesses[4] = true;
		return num;
	}
	
	public int parseMoveCount(String FEN) throws FENException {
		String[] s = FEN.split(" ");
		
		if (s.length < 6) {
			throw new FENException("Missing element(s)", readSuccesses);
		}
		else if (s.length > 6) {
			throw new FENException("Too many elements", readSuccesses);
		}
		
		int num;
		try {
		    num = Integer.parseInt(s[5]);
		} catch (NumberFormatException e) {
			throw new FENException("Invalid number format while parsing Fullmove number: " + e.getMessage(), readSuccesses);
		}

		readSuccesses[5] = true;
		return num;
	}
}
