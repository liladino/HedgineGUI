package chess;

import java.io.PrintStream;

/**
 * A class that represents a chess move. 
 * Consists of two squares (from, to) and a char that represents a promotion.
 * */
public class Move {
	private final Square from;
	private final Square to;
	private final char promotion;
	
	/* * * * * * * *
	 * Constructor *
	 * * * * * * * */
	
	/**
	 * Creates a null move
	 * */
	public Move() {
		//nullmove
		from = new Square();
		to = new Square();
		promotion = ' ';
	}
	
	/**
	 * Creates a move from a strng
	 * @param str should be in long algebraic notation (e.g. e1g1, c7d8q)
	 * */
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
			promotion = str.charAt(4);
		}
		else {
			promotion = ' ';
		}
	}
	
	/**
	 * Creates a move from Squares and a char. 
	 * @param promotion a lowercase character, that the pawn will turn into. If there is no promotion, can be anything but lowercase piece names. The program internally will store a space.
	 * */
	public Move(Square from, Square to, char promotion) {
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
	
	public Move(Move m) {
		from = new Square(m.from);
		to = new Square(m.to);
		promotion = m.promotion;
	}

	/* * * * * *
	 * Getters *
	 * * * * * */
	public Square getFrom() {
		return from;
	}
	public Square getTo() {
		return to;
	}
	public char getPromotion() {
		return promotion;
	}
	
	/* * * * * *
	 * Methods *
	 * * * * * */
	private boolean isMove(String str) {
		//does NOT check if the move is legal, checks only if the string looks like a move 
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
				switch (str.charAt(4)) {
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		
		Move m = (Move)o;
		if (!m.getFrom().equals(from)) return false;
		if (!m.getTo().equals(to)) return false;
		if (m.getPromotion() != promotion) return false;
		return true;
	}
	
	/* * * *
	 * IO  *
	 * * * */
	public void printMove(PrintStream out) {
		out.printf("%c%d%c%d%c", from.getFile(), from.getRank(), to.getFile(), to.getRank(), promotion);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(from.getFile());
		sb.append(from.getRank());
		sb.append(to.getFile());
		sb.append(to.getRank());
		sb.append(promotion);
		return new String(sb);
	}
}
