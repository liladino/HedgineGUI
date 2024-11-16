package chess.IO;

import java.util.ArrayList;

import chess.Board;
import chess.Move;
import chess.Square;
import utility.Result;

public class PGNConverter {
	private PGNConverter(){}

	public static String convertMoveToPGNString(Board board, Move m){
		//this function assumes the move is legal
		
		Board b = new Board(board);

		//check catling
		if (Character.toUpperCase(b.boardAt(m.getFrom())) == 'K' && Math.abs(m.getFrom().getFile() - m.getTo().getFile()) == 2){
			if (m.getTo().getFile() == 'c'){
				return "O-O-O";
			}
			return "O-O";
		}
		
		StringBuilder sb = new StringBuilder();


		if (Character.toUpperCase(b.boardAt(m.getFrom())) != 'P'){
			sb.append(Character.toUpperCase(b.boardAt(m.getFrom())));
			sb.append(getNotPawnMovePGN(b, m));
		}
		else {
			sb.append(getPawnMovePGN(b, m));
		}

		//check if the move results in check / mate
		b.makeMove(m);
		if (b.inCheck()){
			Result temp = b.getResult(); 
			if (temp == Result.WHITE_WON || temp == Result.BLACK_WON){			
				//checkmate	
				sb.append('#');
			}
			else{
				sb.append('+');
			}
		}

		return new String(sb);
	}

	private static String getPawnMovePGN(Board b, Move m){
		//this function assumes the move is legal
		StringBuilder sb = new StringBuilder();

		if (b.boardAt(m.getTo()) != ' ' || b.getEnPassantTarget().equals(m.getTo())){
			sb.append(m.getFrom().getFile());
			sb.append('x');
		}

		sb.append(m.getTo().toString());

		if (m.getPromotion() != ' '){
			sb.append('=');
			sb.append(Character.toUpperCase(m.getPromotion()));
		}
		
		return new String(sb);
	}

	private static String getNotPawnMovePGN(Board b, Move m){
		//this function assumes the move is legal
		StringBuilder sb = new StringBuilder();

		boolean takes = false;
		if (b.boardAt(m.getTo()) != ' '){
			takes = true;
		}

		ArrayList<Square> pieces = getListOfPieces(b, b.boardAt(m.getFrom()));
		Square from = m.getFrom();
		boolean pieceOnSameRank = false;
		boolean pieceOnSameFile = false;
		for (Square s : pieces){
			if (s.equals(from)){
				continue;
			}
			if (b.isMoveLegal(new Move(s, m.getTo(), ' '))){
				if (s.getFile() == from.getFile()){
					pieceOnSameFile = true;
				}
				if (s.getRank() == from.getRank()){
					pieceOnSameRank = true;
				}
			}
		}

		if (pieceOnSameRank){
			sb.append(from.getFile());
		}
		if (pieceOnSameFile){
			sb.append(from.getRank());
		}
		
		if (takes){
			sb.append('x');
		}

		sb.append(m.getTo().toString());

		return new String(sb);
	}

	private static ArrayList<Square> getListOfPieces(Board b, char piece){
		ArrayList<Square> list = new ArrayList<>();
		for (int i = 1; i <= 8; i++){
			for (char j = 'a'; j <= 'h'; j++){
				if (b.boardAt(new Square(j, i)) == piece){
					list.add(new Square(j, i));
				}
			}
		}
		return list;
	}
}
