package core.chess;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;

import core.IO.FENException;
import core.IO.FENManager;
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
	
	private char[][] charBoard;
	private Sides toMove;
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
	public Board(String fen) throws FENException {
		try {
			setupBoard(fen);
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
		charBoard = new char[12][12];
		for (int i = 2; i < 10; i++)
			charBoard[i] = Arrays.copyOf(b.charBoard[i], b.charBoard[i].length);
		
		if (b.toMove == Sides.WHITE) toMove = Sides.WHITE; else toMove = Sides.BLACK;
		
		castlingRights = Arrays.copyOf(b.castlingRights, 4);
		
		enPassantTarget = new Square(b.enPassantTarget);
		fiftyMoveRule = b.fiftyMoveRule;
		fullMoveCount = b.fullMoveCount;
		
		legalMoves = null;
	}
	
	private void setupBoard(String fen) throws FENException {
		FENManager f = new FENManager();
		try {
			charBoard = f.parseBoard(fen);
			toMove = f.parseTomove(fen);
			castlingRights = f.parseCastlingRights(fen);
			enPassantTarget = f.parseEnPassant(fen);
			fiftyMoveRule = f.parseFiftyMoveRule(fen);
			fullMoveCount = f.parseMoveCount(fen);
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
		Sides notToMove = (toMove == Sides.WHITE ? Sides.BLACK : Sides.WHITE);
		if (inCheck(notToMove)) {
			throw new FENException("Illegal board: The side not to move is in check.", 6);
		}
	}
	
	
	/* * * * * *
	 * Getters *
	 * * * * * */

	public Sides tomove() {
		return toMove;
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
	
	public char[][] getRawBoard(){
		return charBoard;
	}
	
	public char boardAt(int row, int col) {
		if (row < 2 || col < 2 || row > 9 || col > 9) {
			return 0;
		}
		return charBoard[row][col];
	}
	
	public char boardAt(Square s) {
		if (s.isNull()) {
			return 0;
		}
		return charBoard[s.getRowCoord()][s.getColCoord()];
	}
	
	public char boardAt(char file, int rank) {
		Square s = new Square(file, rank);
		if (s.isNull()) {
			return 0;
		}
		return charBoard[s.getRowCoord()][s.getColCoord()];
	}
	
	
	
	/* * * * 
	 * IO  *
	 * * * */
	public void printToStream(PrintStream out) {
		printToStream(out, Sides.WHITE);
	}
	
	public void printToStream(PrintStream out, Sides t) {
		out.print("|");
		for (int j = 2; j < 9; j++){
			out.print("---+");
		}
		out.printf("---|%n");
		for (int i = (t == Sides.WHITE ? 9 : 2); i > 1 && i < 10; i += (t == Sides.WHITE ? -1 : 1)){
			out.print("|");
			for (int j = 2; j < 10; j++){
				out.printf(" %c |", charBoard[i][j]);
			}
			out.printf("%n|");
			for (int j = 2; j < 9; j++){
				out.print("---+");
			}
			out.print("---|%n");
		}
		out.print("%n");
	}
	
	@Override
	public String toString() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		PrintStream printStream = new PrintStream(outputStream);
		
		printToStream(printStream);
		
		return outputStream.toString();
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
		if (toMove == Sides.BLACK) toMove = Sides.WHITE;
		else toMove = Sides.BLACK;
	}
	
	/**
	 * @param m tries to make the move
	 * Does NOT check if the move is legal, only if it "looks valid enough" to make it.
	 * If there is an error with the move (is null, no piece stands there), it returns without doing anything.
	 * */
	public void makeMove(Move m) {
		if (m.isNull()) return;
		if (charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == ' ') return;
		
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
		if (charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p'
			&& Math.abs(m.getTo().getRank() - m.getFrom().getRank()) == 2
			&& (charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord() - 1] == 'P'
				|| charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord() + 1] == 'P')) {
			
			enPassantTarget = new Square(m.getTo().getFile(), m.getTo().getRank() + 1);
		}
		else if (charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P'
				&& Math.abs(m.getTo().getRank() - m.getFrom().getRank()) == 2
				&& (charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord() - 1] == 'p'
					|| charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord() + 1] == 'p')) {
				
			enPassantTarget = new Square(m.getTo().getFile(), m.getTo().getRank() - 1);
		}
		else {
			enPassantTarget = new Square();
		}
		
		//making the move
		if (charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] != ' ') {
			fiftyMoveRule = 0;
			//takes
			if (m.getTo().getRank() == 8 && charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P') {
				//white takes and promotes
				if (m.getPromotion() == ' ') charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'Q'; //promotion unspecified
				charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = (char)(m.getPromotion() - 'a' + 'A');
			}
			else if (m.getTo().getRank() == 1 && charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p') {
				//black takes and promotes
				if (m.getPromotion() == ' ') charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'q';
				charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = m.getPromotion();
			}
			else {
				//vanilla taking
				charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
			}
		}
		else if ( (	charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P' && m.getTo().getRank() == 6
					&& charBoard[m.getFrom().getRowCoord()][m.getTo().getColCoord()] == 'p'
					&& charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] == ' ')
				||  
					(charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p' && m.getTo().getRank() == 3
					&& charBoard[m.getFrom().getRowCoord()][m.getTo().getColCoord()] == 'P'
					&& charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] == ' ')) {
			fiftyMoveRule = 0;
			//en passant
			charBoard[m.getFrom().getRowCoord()][m.getTo().getColCoord()] = ' ';
			charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
		}
		else if ((charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'k' || charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'K')
				&& Math.abs(m.getFrom().getColCoord() - m.getTo().getColCoord()) == 2) {
			//castling
			
			//rm rights
			if (m.getFrom().getRank() == 1) {
				castlingRights[0] = castlingRights[1] = false;
			}
			else {
				castlingRights[2] = castlingRights[3] = false;
			}
			charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
			if (m.getTo().getFile() == 'g') {
				charBoard[m.getTo().getRowCoord()][7] = charBoard[m.getFrom().getRowCoord()][9];
				charBoard[m.getFrom().getRowCoord()][9] = ' ';
			}
			else {
				charBoard[m.getTo().getRowCoord()][5] = charBoard[m.getFrom().getRowCoord()][2];
				charBoard[m.getFrom().getRowCoord()][2] = ' ';
			}
			
		}
		else if (m.getTo().getRank() == 8 && charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P') {
			fiftyMoveRule = 0;
			//white promotes
			if (m.getPromotion() == ' ') charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'Q'; //promotion unspecified
			charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = (char)(m.getPromotion() - 'a' + 'A');
		}
		else if (m.getTo().getRank() == 1 && charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p') {
			fiftyMoveRule = 0;
			//black promotes
			if (m.getPromotion() == ' ') charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = 'q';
			charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = m.getPromotion();
		}
		else {
			if (charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'p' || charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] == 'P') {
				fiftyMoveRule = 0;
			}
			charBoard[m.getTo().getRowCoord()][m.getTo().getColCoord()] = charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()];
		}
		
		charBoard[m.getFrom().getRowCoord()][m.getFrom().getColCoord()] = ' ';
		
		//set meta values
		if (toMove == Sides.BLACK) {
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
				if (toMove == Sides.WHITE) return Result.BLACK_WON;
				return Result.WHITE_WON;
			}
			return Result.STALEMATE;
		}
		
		if (sufficientMaterial()) return Result.ONGOING;
		return Result.DRAW;
	}

	private boolean sufficientMaterial(){
		int wKnightCount = 0;
		int wBishopCount = 0;
		int bKnightCount = 0;
		int bBishopCount = 0;
		for (int i = 2; i < 10; i++) {
			for (int j = 2; j < 10; j++) {
				switch (charBoard[i][j]) {
					case 'R', 'r', 'Q', 'q', 'P', 'p': return true;
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
					default:
						//empty
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
				if ((current == Sides.WHITE && charBoard[i][j] >= 'A' && charBoard[i][j] <= 'Z' && charBoard[i][j] != 'K')
					|| (current == Sides.BLACK && charBoard[i][j] >= 'a' && charBoard[i][j] <= 'z' && charBoard[i][j] != 'K')){
					//check if the active side has anything but a king. This is needed to be checked if the time is up
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean inCheck() {
		return inCheck(toMove);
	}
	public boolean inCheck(Sides tomove) {
		int kingi = 0;
		int kingj = 0;
		for (int i = 2; i < 10; i++) {
			for (int j = 2; j < 10; j++) {
				if ((charBoard[i][j] == 'K' && tomove == Sides.WHITE) || (charBoard[i][j] == 'k' && tomove == Sides.BLACK)){
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
				if (charBoard[kingi + i][kingj + 2 * j] + colorOffset == 'n') { return true; }
			}
		}
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				if (charBoard[kingi + 2 * i][kingj + j] + colorOffset == 'n') { return true; }
			}
		}
		
		//bishop directions
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				int k = 1;
				while (charBoard[kingi + k * i][kingj + k * j] == ' ') { k++; }
				
				if (charBoard[kingi + k * i][kingj + k * j] + colorOffset == 'b'
					|| charBoard[kingi + k * i][kingj + k * j] + colorOffset == 'q' ) { return true; }
			}
		}
		
		//rook direction
		for (int i = -1; i <= 1; i += 2) {
			int k = 1;
			while (charBoard[kingi + k * i][kingj] == ' ') { k++; }
			
			if (charBoard[kingi + k * i][kingj] + colorOffset == 'r'
				|| charBoard[kingi + k * i][kingj] + colorOffset == 'q' ) { return true; }
		}
		for (int i = -1; i <= 1; i += 2) {
			int k = 1;
			while (charBoard[kingi][k * i + kingj] == ' ') { k++; }
			
			if (charBoard[kingi][k * i + kingj] + colorOffset == 'r'
				|| charBoard[kingi][k * i + kingj] + colorOffset == 'q' ) { return true; }
		}
		
		//pawns
		if (tomove == Sides.WHITE) {
			if (charBoard[kingi + 1][kingj + 1] == 'p' || charBoard[kingi + 1][kingj - 1] == 'p') { return true; }
		}
		else {
			if (charBoard[kingi - 1][kingj + 1] == 'P' || charBoard[kingi - 1][kingj - 1] == 'P') { return true; }
		}
		
		//kings can't get near each other, so let's test it by calling it a check
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == j && j == 0) { continue; }
				if (charBoard[kingi + i][kingj + j] + colorOffset == 'k') return true;
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
		System.out.printf("Number of legal moves %d plies deep: %d%n", depth, r);
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

