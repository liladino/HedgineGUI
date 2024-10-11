package chess;

import java.io.PrintStream;

public class Move {
	private final Square from;
	private final Square to;
	private final char promotion;
	
	public Move() {
		//nullmove
		from = new Square();
		to = new Square();
		promotion = ' ';
	}
	
	public Move(String str) {
		if (!(this.isMove(str))) {
			from = new Square();
			to = new Square();
			promotion = ' ';
			return;
		}
		
		char file = str.charAt(0);
		int rank = str.charAt(1) - '0';
		from = new Square(file, rank);
		
		file = str.charAt(2);
		rank = str.charAt(3) - '0';
		to = new Square(file, rank);
		
		if (str.length() == 5) {
			promotion = str.charAt(5);
		}
		else {
			promotion = ' ';
		}
	}
	
	public Move(Square from, Square to, char promotion) {
		//nullmove
		this.from = from;
		this.to = to;
		switch (promotion) {
			case 'q':
				this.promotion = promotion;
				break;
			case 'r':
				this.promotion = promotion;
				break;
			case 'n':
				this.promotion = promotion;
				break;
			case 'b':
				this.promotion = promotion;
				break;
			default:
				this.promotion = ' ';
				break;
		}
	}
	
	boolean isMove(String str) {
		//does NOT check if the move is legal, only if it looks like a move based on a string
		if (!(str.length() == 4 || str.length() == 5)) return false;
		char file = str.charAt(0);
		int rank = str.charAt(1) - '0';
		Square fr = new Square(file, rank);
		
		if (fr.isNull()) return false;
		
		file = str.charAt(2);
		rank = str.charAt(3) - '0';
		Square t = new Square(file, rank);
		
		if (t.isNull()) return false;
		
		if (str.length() == 5) {
				switch (str.charAt(5)) {
				case 'q':
					break;
				case 'r':
					break;
				case 'n':
					break;
				case 'b':
					break;
				case ' ':
					break;
				default:
					return false;
			}
		}
		return true;
	}
	
	public boolean isNull() {
		if (from.isNull()) return true;
		if (to.isNull()) return true;
		return false;
	}
	
	public void printMove(PrintStream out) {
		out.printf("%c%d%c%d%c", from.getFile(), from.getRank(), to.getFile(), to.getRank(), promotion);
	}
	
	public Square getFrom() {
		return from;
	}
	public Square getTo() {
		return to;
	}
	public char getPromotion() {
		return promotion;
	}
}
