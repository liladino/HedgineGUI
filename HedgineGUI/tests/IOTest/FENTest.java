package IOTest;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import chess.Board;
import chess.IO.FENException;

class FENTest {
	Board start;
	Board a;
	Board b;
	Board c;
	Board d;
	Board badFEN;
	
	@Test
	void testRandomPositionConversions() {
		assertDoesNotThrow(() -> a = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K b - - 0 1"));
		assertEquals(a.convertToFEN(), "2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K b - - 0 1");

		assertDoesNotThrow(() -> b = new Board("rnb1k2r/pp2b1pp/2pp1nq1/8/3Pp3/2N1NP2/PPP1B1PP/R1BQ1RK1 b kq - 0 10"));
		assertEquals(b.convertToFEN(), "rnb1k2r/pp2b1pp/2pp1nq1/8/3Pp3/2N1NP2/PPP1B1PP/R1BQ1RK1 b kq - 0 10");

		assertDoesNotThrow(() -> c = new Board("r1bq1r2/pp2n3/4N2k/3pPppP/1b1n2Q1/2N5/PP3PP1/R1B1K2R w KQ g6 0 15"));
		assertEquals(c.convertToFEN(), "r1bq1r2/pp2n3/4N2k/3pPppP/1b1n2Q1/2N5/PP3PP1/R1B1K2R w KQ g6 0 15");

		assertDoesNotThrow(() -> d = new Board("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2 10"));
		assertEquals(d.convertToFEN(), "r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2 10");
		
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/2pRnr1P/"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/"));
		assertThrows(FENException.class, () -> badFEN = new Board("2x5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/p2NBp1p/1bp1nPPr/3k4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K"));
		assertThrows(FENException.class, () -> badFEN = new Board("9/p2NBp1p/1bp1nPPr/3k4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b6/p2NBp1p/1bp1nPPr/3p4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K c"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K w a"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K w KQkqq"));
		assertThrows(FENException.class, () -> badFEN = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K w - e9"));
		
		assertDoesNotThrow(() -> d = new Board("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq -"));
		assertEquals(d.getFiftyMoveRule(), 0);
		assertEquals(d.getFullMoveCount(), 1);
		
		assertDoesNotThrow(() -> d = new Board("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2"));
		assertEquals(d.getFiftyMoveRule(), 2);
		assertEquals(d.getFullMoveCount(), 1);
		assertEquals(d.convertToFEN(), "r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2 1");
	}
	
	
	@Test
	void testConvertToFEN() {
		start = new Board();
		assertEquals(start.getFiftyMoveRule(), 0);
		assertEquals(start.getFullMoveCount(), 1);
		assertEquals(start.convertToFEN(), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	}

}
