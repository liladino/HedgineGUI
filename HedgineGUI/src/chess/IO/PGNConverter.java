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
			switch (Character.toUpperCase(b.boardAt(m.getFrom()))){
				case 'R':
					sb.append(getRookMovePGN(b, m));
					break;
				default:
					return null;
			}
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

	private static String getRookMovePGN(Board b, Move m){
		//this function assumes the move is legal
		StringBuilder sb = new StringBuilder();

		boolean takes = false;
		if (b.boardAt(m.getTo()) != ' '){
			takes = true;
		}

		ArrayList<Square> rooks = getListOfPieces(b, b.boardAt(m.getFrom()));

		Square from = m.getFrom();
		boolean pieceOnSameRank = false;
		boolean pieceOnSameFile = false;
		//Board copBoard = new Board(b);
		for (Square s : rooks){
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
		
		return new String(sb);
	}

	private static ArrayList<Square> getListOfPieces(Board b, char piece){
		ArrayList<Square> list = new ArrayList<>();
		for (int i = 1; i <= 8; i++){
			for (char j = 'a'; j <= 'h'; j++){
				if (b.boardAt(new Square(i, j)) == piece){
					list.add(new Square(i, j));
				}
			}
		}
		return list;
	}
}
