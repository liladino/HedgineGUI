package chess;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;

import chess.IO.FENException;
import chess.IO.FENManager;
import utility.Result;
import utility.Sides;

/**
 * This class represents a chessboard, and all the information regarding it.
 * It handles making a move on the board, getting the legal moves and setting the metadata:
 *  - which side is to move
 *  - castling rights
 *  - en passant possibility
 *  - fifty move rule count
 *  - full move count
 *  
 * Note: the makeMove() method *does not* check if the move is actually legal.
 * */
public class Board implements Serializable {
	private static final long serialVersionUID = 1871341340688870L;
	
	private char[][] board;
	private Sides tomove;
	private boolean[] castlingRights; //white kingside, queenside, black kingside, queenside
	private Square enPassantTarget;
	private int fiftyMoveRule;
	private int fullMoveCount;
	
	private transient LegalMoves legalMoves = null;
	
	
	/* * * * * * * * *
	 * Constructors  *
	 * * * * * * * * */
	/**
	 * Tries to set up a board from a FEN.
	 * If the last move number and fifty move rule fields are not found, they are set to 1 and 0 respectively.
	 * If other fields are missing/are invalid, throws a FENException.
	 * */
	public Board(String FEN) throws FENException {
		try {
			setupBoard(FEN);
		}
		catch (FENException e) {
			throw(e);
		}
	}
	
	public Board() {
		try {
			//startpos
			setupBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		}
		catch (FENException e) {
			//this can't throw an exception if i didn't mess up
			System.out.println(e);
		}
	}
	
	public Board(Board b) {
		board = new char[12][12];
		for (int i = 2; i < 10; i++) 
			for (int j = 2; j < 10; j++) 
				board[i][j] = b.board[i][j];
		
		if (b.tomove == Sides.WHITE) tomove = Sides.WHITE; else tomove = Sides.BLACK;
		
		castlingRights = Arrays.copyOf(b.castlingRights, 4);
		
		enPassantTarget = new Square(b.enPassantTarget);
		fiftyMoveRule = b.fiftyMoveRule;
		fullMoveCount = b.fullMoveCount;
		
		legalMoves = null;
	}
	
	private void setupBoard(String FEN) throws FENException {
		FENManager f = new FENManager();
		try {
			board = f.parseBoard(FEN);
			tomove = f.parseTomove(FEN);
			castlingRights = f.parseCastlingRights(FEN);
			enPassantTarget = f.parseEnPassant(FEN);
			fiftyMoveRule = f.parseFiftyMoveRule(FEN);
			fullMoveCount = f.parseMoveCount(FEN);
		}
		catch (FENException e) {
			if (e.getSuccesfulFields() < 4) {
				throw new FENException(e.getMessage() + " can't parse FEN", e.getSuccesfulFields());
			}
			//non-fatal:
			System.out.print(e.getMessage() + ": ");			
			if (e.getSuccesfulFields() == 4) {
				System.out.print("fifty move rule counter is set to 0, ");
				fiftyMoveRule = 0;
			}
			System.out.println("fullmove number is set to 1.");
			fullMoveCount = 1;
		}
		Sides notToMove = (tomove == Sides.WHITE ? Sides.BLACK : Sides.WHITE);
		if (inCheck(notToMove)) {
			throw new FENException("Illegal board: The side not to move is in check.", 6);
		}
	}
	
	
	/* * * * * *
	 * Getters *
	 * * * * * */

	public Sides tomove() {
		return tomove;
	}

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
	
	public Square getEnPassantTarget() {
		return enPassantTarget;
	}
	
	public int getFiftyMoveRule() {
		return fiftyMoveRule;
	}
	
	public int getFullMoveCount() {
		return fullMoveCount;
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
		printToStream(out, Sides.WHITE);
	}
	
	public void printToStream(PrintStream out, Sides t) {
		out.printf("|");
		for (int j = 2; j < 9; j++){
			out.printf("---+");
		}
		out.printf("---|\n");
		for (int i = (t == Sides.WHITE ? 9 : 2); i > 1 && i < 10; i += (t == Sides.WHITE ? -1 : 1)){
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
	
	@Override
	public String toString() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        PrintStream printStream = new PrintStream(outputStream);
        
        printToStream(printStream);
        
        String result = outputStream.toString();
        return result;
	}
	
	public String convertToFEN() {
		FENManager f = new FENManager();
		return f.convertToFEN(this);
	}
	
	public void printLegalMoves() {
		if (legalMoves == null) {
			generateLegalMoves();
		}
		System.out.println(legalMoves);
	}
	
	/* * * * *
	 * Moves *
	 * * * * */
	private void switchColor() {
		if (tomove == Sides.BLACK) tomove = Sides.WHITE;
		else tomove = Sides.BLACK;
	}
	
	/**
	 * @param m tries to make the move
	 * Does NOT check if the move is legal, only if it "looks valid enough" to make it.
	 * If there is an error with the move (is null, no piece stands there), it returns without doing anything.
	 * */
	public void makeMove(Move m) {
		if (m.isNull()) return;
		if (board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == ' ') return;
		
		legalMoves = null;
		
		int tempFiftyMove = fiftyMoveRule;
		
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
		else if ( (	board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P' && m.getTo().getRank() == 6 
					&& board[m.getFrom().getRowCoord()][m.getTo().getColCoord()] == 'p'
					&& board[m.getTo().getRowCoord()][m.getTo().getColCoord()] == ' ') 
				||  
					(board[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p' && m.getTo().getRank() == 3 
					&& board[m.getFrom().getRowCoord()][m.getTo().getColCoord()] == 'P'
					&& board[m.getTo().getRowCoord()][m.getTo().getColCoord()] == ' ')) {
			fiftyMoveRule = 0;
			//en passant
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
		
		//set meta values
		if (tomove == Sides.BLACK) {
			fullMoveCount++;
			if (tempFiftyMove == fiftyMoveRule) {
				fiftyMoveRule++;
			}
		}
		
		switchColor();
	}
	
	
	/* * * * * * * *
	 * Legal Moves *
	 * * * * * * * */
	public int generateLegalMoves() {
		legalMoves = new LegalMoves(this);
		return legalMoves.size();
	}
	
	public boolean isMoveLegal(Move m) {
		if (legalMoves == null) {
			generateLegalMoves();
		}
		return legalMoves.contains(m);
	}
	
	public int numberOfLegalMoves() {
		if (legalMoves == null) {
			return generateLegalMoves();
		}
		return legalMoves.size();
	}
	
	public Move getLegalMove(int i) {
		return legalMoves.get(i);
	}
	
	public Result getResult() {
		if (fiftyMoveRule >= 50) {
			return Result.DRAW;
		}
		
		if (legalMoves == null) {
			generateLegalMoves();
		}
		
		if (legalMoves.isEmpty()) {
			if (inCheck()) {
				if (tomove == Sides.WHITE) return Result.BLACK_WON;
				return Result.WHITE_WON;
			}
			return Result.STALEMATE;
		}
		
		if (sufficientMaterial()) return Result.ONGOING;
		return Result.DRAW;
	}

	private boolean sufficientMaterial(){
		int wKnightCount = 0, wBishopCount = 0, bKnightCount = 0, bBishopCount = 0;
		for (int i = 2; i < 10; i++) {
			for (int j = 2; j < 10; j++) {
				switch (board[i][j]) {
					case 'R': return true;
					case 'Q': return true;
					case 'q': return true;
					case 'r': return true;
					case 'P': return true;
					case 'p': return true;
					case 'N': 
						wKnightCount++;
						break;
					case 'B': 
						wBishopCount++;
						break;
					case 'n': 
						bKnightCount++;
						break;
					case 'b': 
						bBishopCount++;
						break;
				}
				if (wKnightCount + wBishopCount + bKnightCount + bBishopCount > 1) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean sufficientMaterial(Sides current){
		for (int i = 2; i < 10; i++) {
			for (int j = 2; j < 10; j++) {
				if ((current == Sides.WHITE && board[i][j] >= 'A' && board[i][j] <= 'Z' && board[i][j] != 'K') 
					|| (current == Sides.BLACK && board[i][j] >= 'a' && board[i][j] <= 'z' && board[i][j] != 'K')){
					//check if the active side has anything but a king. This is needed to be checked if the time is up
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean inCheck() {
		return inCheck(tomove);
	}
	public boolean inCheck(Sides tomove) {
		int kingi = 0, kingj = 0;
		for (int i = 2; i < 10; i++) {
			for (int j = 2; j < 10; j++) {
				if ((board[i][j] == 'K' && tomove == Sides.WHITE) || (board[i][j] == 'k' && tomove == Sides.BLACK)){
					kingi = i;
					kingj = j;
				}
			}
		}
		if (kingi * kingj == 0) {
			//can't reach this if everything goes well: only made legal moves and the startpos was legal
			return false;
		}
		
		//to check if square + offset is q,r,b,n, or p
		int colorOffset = (tomove == Sides.WHITE ? 0 : 'a' - 'A');
		
		
		//knight directions
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				if (board[kingi + i][kingj + 2 * j] + colorOffset == 'n') { return true; }
			}
		}
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				if (board[kingi + 2 * i][kingj + j] + colorOffset == 'n') { return true; }
			}
		}
		
		//bishop directions
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				int k = 1;
				while (board[kingi + k * i][kingj + k * j] == ' ') { k++; }
				
				if (board[kingi + k * i][kingj + k * j] + colorOffset == 'b'
					|| board[kingi + k * i][kingj + k * j] + colorOffset == 'q' ) { return true; }
			}
		}
		
		//rook direction
		for (int i = -1; i <= 1; i += 2) {
			int k = 1;
			while (board[kingi + k * i][kingj] == ' ') { k++; }
			
			if (board[kingi + k * i][kingj] + colorOffset == 'r'
				|| board[kingi + k * i][kingj] + colorOffset == 'q' ) { return true; }
		}
		for (int i = -1; i <= 1; i += 2) {
			int k = 1;
			while (board[kingi][k * i + kingj] == ' ') { k++; }
			
			if (board[kingi][k * i + kingj] + colorOffset == 'r'
				|| board[kingi][k * i + kingj] + colorOffset == 'q' ) { return true; }
		}
		
		//pawns
		if (tomove == Sides.WHITE) {
			if (board[kingi + 1][kingj + 1] == 'p' || board[kingi + 1][kingj - 1] == 'p') { return true; }
		}
		else {
			if (board[kingi - 1][kingj + 1] == 'P' || board[kingi - 1][kingj - 1] == 'P') { return true; }
		}
		
		//kings can't get near each other, so let's test it by calling it a check
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == j && j == 0) { continue; }
				if (board[kingi + i][kingj + j] + colorOffset == 'k') return true;
			}
		}
		
		return false;
	}
	
	public int perfTest(int depth) {
		return perfTest(depth, false);
	}
	
	public int perfTest(int depth, boolean print) {
		if (!print) {
			return recursiveLegalMoves(depth, this);
		}
		int r = recursiveLegalMoves(depth, this);
		System.out.println(this);
		System.out.printf("Number of legal moves %d plies deep: %d\n", depth, r);
		return r;
	}
	
	private static int recursiveLegalMoves(int depth, final Board b) {
		if (depth <= 0) {
			return 1;
		}
		
		Board temp = new Board(b);
		
		temp.generateLegalMoves();
		int count = 0;
		
		for (Move i : temp.legalMoves) {
			temp.makeMove(i);
			count += recursiveLegalMoves(depth - 1, temp);
			temp = new Board(b);
		}
		return count;
	}
	
	
}

