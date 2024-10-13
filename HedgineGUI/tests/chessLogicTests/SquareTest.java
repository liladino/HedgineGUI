package chessLogicTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.Square;

class SquareTest {
	private Square nullSquare, e4, h8;
	
	@BeforeEach
	void setUpAll() {
		nullSquare = new Square();
		e4 = new Square('e', 4);
		h8 = new Square(9, 9);
	}
	
	@Test
	void testConstructor() {
		//no args
		assertTrue(nullSquare.isNull());
		//from file, rank
		assertTrue(!e4.isNull());
		//from coordinates
		assertTrue(!h8.isNull());
	}
	
	@Test
	void testGetters() {
		assertEquals(nullSquare.getRank(), -1);
		assertEquals(nullSquare.getFile(), 0);
		assertEquals(nullSquare.getColCoord(), -1);
		assertEquals(nullSquare.getRowCoord(), -1);
		
		assertEquals(e4.getFile(), 'e');
		assertEquals(e4.getRank(), 4);
		assertEquals(e4.getColCoord(), 6);
		assertEquals(e4.getRowCoord(), 5);	
	}
	
	@Test
	void testEquals() {
		Square s = new Square(5, 6);
		assertTrue(!h8.equals(s));
	}
}
