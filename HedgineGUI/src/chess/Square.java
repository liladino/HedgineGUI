package chess;

public class Square {
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
	
	public Square(char file, int rank) {
		if (file < 'a' || file > 'h' || rank < 1 || rank > 8) {
			rank = -1;
			file = 0;
			return;
		}
		this.rank = rank;
		this.file = file;
	}
	
	//constructor for 12x12 board coordinates
	public Square(int row, int col) {
		rank = row - 1;
		file = (char)(col + 'a' - 2);
	}
	
	/* * * * * *
	 * Getters *
	 * * * * * */
	
	//get coordinates for the 12x12 array
	int getRowCoord() {
		if (rank == -1) return -1;
		return rank + 1;
	}
	int getColCoord() {
		if (file == 0) return -1;
		return file - 'a' + 2;
	}
	
	int getRank() {
		return rank;
	}
	char getFile() {
		return file;
	}

	/* * * * * *
	 * Methods *
	 * * * * * */
	public boolean isNull() {
		if (rank == -1 || rank == 0) {
			return true;
		}
		return false;
	}
	
	public boolean equals(Square s) {
		if (s.getFile() != file) {
			return false;
		}
		if (s.getRank() != rank) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(file);
		sb.append(rank);
		return new String(sb);
	}
}
