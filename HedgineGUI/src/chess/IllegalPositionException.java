package chess;

public class IllegalPositionException extends Exception {
	private static final long serialVersionUID = 2L;
	public IllegalPositionException(String message) {
		super(message);
	}
}
