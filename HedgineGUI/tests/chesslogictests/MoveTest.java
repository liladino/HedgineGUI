package chesslogictests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.Move;
import chess.Square;

class MoveTest {
	Move nm, g1f3, e7e8q, a1a2, badString; 
	
	@BeforeEach
	void setUp() {
		nm = new Move();
		
		Square g1 = new Square('g', 1);
		Square f3 = new Square('f', 3);
		g1f3 = new Move(g1, f3, ' ');
		
		e7e8q = new Move("e7e8q");
		
		a1a2 = new Move("a1a2");
		
		badString = new Move("Szavak");
	}

	@Test
	void testConstructors() {
		assertTrue(nm.isNull());
		assertTrue(badString.isNull());
		Move ng1f3 = new Move(g1f3);
		assertEquals(ng1f3, g1f3);
		assertFalse(g1f3.isNull());
		assertFalse(a1a2.isNull());
		assertFalse(e7e8q.isNull());
		assertTrue(badString.isNull());
	}

	@Test
	void testStrings(){
		assertEquals("g1f3 ", g1f3.toString());
		assertEquals("e7e8q", e7e8q.toString());	
	}
}
