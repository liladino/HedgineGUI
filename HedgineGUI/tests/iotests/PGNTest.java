package iotests;

import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeNoException;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import chess.Board;
import chess.Move;
import chess.Square;
import chess.IO.PGNConverter;


public class PGNTest {
	Board startBoard;
	Board middleGame; //TODO: let this have: en passant, both sides castling, promotion
	Board endGame; //TODO this should have moves like Qa1xe4, or Nbd7, and pins

	@BeforeEach
	void init(){
		startBoard = new Board();
	}

	@Test
	void testPawnMoves(){
		Move e4 = new Move(new Square('e', 2), new Square('e', 4), ' ');	
		assertEquals("e4", PGNConverter.convertMoveToPGNString(startBoard, e4));
		startBoard.makeMove(e4);
		
		Move e6 = new Move(new Square('e', 7), new Square('e', 6), ' ');
		assertEquals("e6", PGNConverter.convertMoveToPGNString(startBoard, e6));
		startBoard.makeMove(e6);

		Move e5 = new Move(new Square('e', 4), new Square('e', 5), ' ');
		assertEquals("e5", PGNConverter.convertMoveToPGNString(startBoard, e5));
		startBoard.makeMove(e5);

		Move d5 = new Move(new Square('d', 7), new Square('d', 5), ' ');
		assertEquals("d5", PGNConverter.convertMoveToPGNString(startBoard, d5));
		startBoard.makeMove(d5);

		Move ed6 = new Move(new Square('e', 5), new Square('d', 6), ' ');
		assertEquals("exd6", PGNConverter.convertMoveToPGNString(startBoard, ed6));
		
	}
}
