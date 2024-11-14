package chess.IO;

import chess.Board;
import chess.Move;

public class PGNConverter {
	private PGNConverter(){}

	public static String convertMoveToPGNString(Board b, Move m){
		//this function assumes the move is legal
		if (Character.toUpperCase(b.boardAt(m.getFrom())) == 'K' && Math.abs(m.getFrom().getFile() - m.getTo().getFile()) == 2){
			//castling
			if (m.getTo().getFile() == 'c'){
				return "O-O-O";
			}
			return "O-O";
		}
		
		StringBuilder sb = new StringBuilder();


		if (Character.toUpperCase(b.boardAt(m.getFrom())) != 'P'){
			sb.append(Character.toUpperCase(b.boardAt(m.getFrom())));
		}
		else {
			return getPawnMovePGN(b, m);
		}
		//TODO other pieces

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
		
		return new String(sb);
	}
}
