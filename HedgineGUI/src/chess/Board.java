package chess;

import java.io.PrintStream;

public class Board {
	private char[][] board = {
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
	private boolean[] castlingRights = 	{ true, true, true, true}; 
										//white kingside, queenside, black kingside, queenside
	private Square enPassantTarget;
	
	private int fiftyMoveRule;
	
	Sides tomove;
	
	
	/* * * * * * * * *
	 * Constructors  *
	 * * * * * * * * */
	
	public Board() {
		enPassantTarget = new Square();
		fiftyMoveRule = 0;
	}
	
	/*public Board(String fen) {
		chessBoard = new char[12][12];
	}*/
	
	/* * * * * *
	 * Getters *
	 * * * * * */

	public boolean wKingSideCastling() {
		return castlingRights[0];
	}
	
	public boolean wQueenSideCastling() {
		return castlingRights[1];
	}
	
	public boolean bKingSideCastling() {
		return castlingRights[2];
	}
	
	public boolean bQueenSideCastling() {
		return castlingRights[3];
	}
	
	public Square enPassantTarget() {
		return enPassantTarget;
	}
	
	public int getFiftyMoveRule() {
		return fiftyMoveRule;
	}
	
	public char boardAt(int row, int col) {
		if (row < 2 || col < 2 || row > 9 || col > 9) {
			return 0;
		}
		return board[row][col];
	}
	
	public char boardAt(Square s) {
		if (s.isNull()) {
			return 0;
		}
		return board[s.getRowCoord()][s.getColCoord()];
	}
	
	public char boardAt(char file, int rank) {
		Square s = new Square(file, rank);
		if (s.isNull()) {
			return 0;
		}
		return board[s.getRowCoord()][s.getColCoord()];
	}
	
	
	
	/* * * * 
	 * IO  *
	 * * * */
	public void printToStream(PrintStream out) {
		out.printf("|");
		for (int j = 2; j < 9; j++){
			out.printf("---+");
		}
		out.printf("---|\n");
		
		for (int i = 9; i > 1; i--){
			out.printf("|");
			for (int j = 2; j < 10; j++){
				out.printf(" %c |", board[i][j]);
			}
			out.printf("\n|");
			for (int j = 2; j < 9; j++){
				out.printf("---+");
			}
			out.printf("---|\n");
		}
		out.printf("\n");
	}
	
	public void printToStream(PrintStream out, Sides t) {
		out.printf("|");
		for (int j = 2; j < 9; j++){
			out.printf("---+");
		}
		out.printf("---|\n");
		for (int i = (t == Sides.white ? 9 : 2); i > 1 && i < 10; i += (t == Sides.white ? -1 : 1)){
			out.printf("|");
			for (int j = 2; j < 10; j++){
				out.printf(" %c |", board[i][j]);
			}
			out.printf("\n|");
			for (int j = 2; j < 9; j++){
				out.printf("---+");
			}
			out.printf("---|\n");
		}
		out.printf("\n");
	}
	
	
	/* * * * *
	 * Moves *
	 * * * * */
	public void makeMove(Move m) {
		if (m.isNull()) return;
		if (board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == ' ') return;
		
		fiftyMoveRule++;
		
		//setting castling rights
		//if anything moves to one of the corners, no castling that way
		if (m.getTo().getRank() == 1) {
			if (m.getTo().getFile() == 'a') castlingRights[1] = false;
			else if (m.getTo().getFile() == 'h') castlingRights[0] = false;
		}
		else if (m.getTo().getRank() == 8) {
			if (m.getTo().getFile() == 'a') castlingRights[3] = false;
			else if (m.getTo().getFile() == 'h') castlingRights[2] = false;
		}

		//if anything moves from one of the corners, no castling that way
		if (m.getFrom().getRank() == 1) {
			if (m.getFrom().getFile() == 'a') castlingRights[1] = false;
			else if (m.getFrom().getFile() == 'h') castlingRights[0] = false;
		}
		else if (m.getFrom().getRank() == 8) {
			if (m.getFrom().getFile() == 'a') castlingRights[3] = false;
			else if (m.getFrom().getFile() == 'h') castlingRights[2] = false;
		}
		
		//setting up en passant
		if (board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p'
			&& Math.abs(m.getTo().getRank() - m.getFrom().getRank()) == 2
			&& (board[m.getTo().getRowCoord()][m.getTo().getColCoord() - 1] == 'P' 
				|| board[m.getTo().getRowCoord()][m.getTo().getColCoord() + 1] == 'P')) {
			
			enPassantTarget = new Square(m.getTo().getFile(), m.getTo().getRank() + 1);
		}
		else if (board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P'
				&& Math.abs(m.getTo().getRank() - m.getFrom().getRank()) == 2
				&& (board[m.getTo().getRowCoord()][m.getTo().getColCoord() - 1] == 'p' 
					|| board[m.getTo().getRowCoord()][m.getTo().getColCoord() + 1] == 'p')) {
				
			enPassantTarget = new Square(m.getTo().getFile(), m.getTo().getRank() - 1);
		}
		else {
			enPassantTarget = new Square();
		}
		
		//making the move
		if (board[m.getTo().getRowCoord()][m.getTo().getColCoord()] != ' ') {
			fiftyMoveRule = 0;
			//takes
			if (m.getTo().getRank() == 8 && board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P') {
				//white takes and promotes
				if (m.getPromotion() == ' ') board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'Q'; //promotion unspecified
				board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = (char)(m.getPromotion() - 'a' + 'A');
			}
			else if (m.getTo().getRank() == 1 && board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p') {
				//black takes and promotes
				if (m.getPromotion() == ' ') board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'q';
				board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = m.getPromotion();
			}
			else {
				//vanilla taking
				board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
			}
		}
		else if (board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P' && m.getTo().getRank() == 6 
				&& board[m.getFrom().getRowCoord()][m.getTo().getColCoord()] == 'p'
				&& board[m.getTo().getRowCoord()][m.getTo().getColCoord()] == ' ') {
			fiftyMoveRule = 0;
			//en passant white
			board[m.getFrom().getRowCoord()][m.getTo().getColCoord()] = ' ';
			board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
		}
		else if (board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p' && m.getTo().getRank() == 3 
				&& board[m.getFrom().getRowCoord()][m.getTo().getColCoord()] == 'P'
				&& board[m.getTo().getRowCoord()][m.getTo().getColCoord()] == ' ') {
			fiftyMoveRule = 0;
			//en passant black
			board[m.getFrom().getRowCoord()][m.getTo().getColCoord()] = ' ';
			board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
		}
		else if ((board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'k' || board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'K')
				&& Math.abs(m.getFrom().getColCoord() - m.getTo().getColCoord()) == 2) {
			//castling
			
			//rm rights
			if (m.getFrom().getRank() == 1) {
				castlingRights[0] = castlingRights[1] = false;
			}
			else {
				castlingRights[2] = castlingRights[3] = false;
			}
			board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
			if (m.getTo().getFile() == 'g') {
				board[m.getTo().getRowCoord()][7] = board[m.getFrom().getRowCoord()][9];
				board[m.getFrom().getRowCoord()][9] = ' ';
			}
			else {
				board[m.getTo().getRowCoord()][5] = board[m.getFrom().getRowCoord()][2];
				board[m.getFrom().getRowCoord()][2] = ' ';
			}
			
		}
		else if (m.getTo().getRank() == 8 && board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P') {
			fiftyMoveRule = 0;
			//white promotes
			if (m.getPromotion() == ' ') board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'Q'; //promotion unspecified
			board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = (char)(m.getPromotion() - 'a' + 'A');
		}
		else if (m.getTo().getRank() == 1 && board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p') {
			fiftyMoveRule = 0;
			//black promotes
			if (m.getPromotion() == ' ') board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'q';
			board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = m.getPromotion();
		}
		else {
			if (board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p' || board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P') {
				fiftyMoveRule = 0;
			}
			board[m.getTo().getRowCoord()][m.getTo().getColCoord()] = board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
		}
		
		board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] = ' ';
	}
	
	
}
