package hedgineGUI.core.chess;

import java.io.Serializable;

/**
 * A field on the board. Provides automatic conversion between the indices used by the Board and user notations.
 * */
public class Square implements Serializable{
	private static final long serialVersionUID = 81867168718303L;

	private char file;
	private int rank;
	
	/* * * * * * * *
	 * Constructor *
	 * * * * * * * */
	
	public Square() {
		rank = -1;
		file = 0;
		//null square
	}
	
	/**
	 * Constructor from user-friendly coordinates
	 * @param file
	 * @param rank
	 */
	public Square(char file, int rank) {
		if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
			this.rank = -1;
			this.file = 0;
			return;
		}
		this.rank = rank;
		this.file = file;
	}

	/**
	 * Constructor from user-friendly coordinates
	 * @param square > string, like e4
	 */
	public Square(String square) {
		char f = ' ';
		int r = ' ';
		if (square.length() == 2) {
			f = square.charAt(0);
			r = square.charAt(1)-'0';
		}
		this(f, r);
	}
	
	/**
	 * constructor for 12x12 board coordinates
	 */
	public Square(int row, int col) {
		if (row < 2 || row > 9 || col < 2 || col > 9) {
			rank = -1;
			file = 0;
			return;
		}
		rank = row - 1;
		file = (char)(col + 'a' - 2);
	}
	
	//copy
	public Square(Square s) {
		rank = s.rank;
		file = s.file;
	}
	
	/* * * * * *
	 * Getters *
	 * * * * * */
	
	//get coordinates for the 12x12 array
	public int getRowCoord() {
		if (rank == -1) return -1;
		return rank + 1;
	}
	public int getColCoord() {
		if (file == 0) return -1;
		return file - 'a' + 2;
	}
	
	public int getRank() {
		return rank;
	}
	public char getFile() {
		return file;
	}

	/* * * * * *
	 * Methods *
	 * * * * * */
	/**
	 * Checks if it's a nullmove
	 */
	public boolean isNull() {
		return (rank == -1 || rank == 0);
	}

	@Override
	public int hashCode(){
		return (file-'a') * 100 + rank;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Square s)) return false;
		return !(s.getFile() != file || s.getRank() != rank);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(file);
		sb.append(rank);
		return new String(sb);
	}
}
