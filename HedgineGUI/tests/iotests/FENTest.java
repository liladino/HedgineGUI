package iotests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import core.IO.FENException;
import core.chess.Board;

class FENTest {
	Board start;
	Board a;
	Board b;
	Board c;
	Board d;
	Board badFEN;

	@Test
	void testRandomPositionConversions()
	{
		assertDoesNotThrow(() -> a = new Board("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K b - - 0 1"));
		assertEquals("2b5/p2NBp1p/1bp1nPPr/3P4/2pRnr1P/1k1B1Ppp/1P1P1pQP/Rq1N3K b - - 0 1", a.convertToFEN());

		assertDoesNotThrow(() -> b = new Board("rnb1k2r/pp2b1pp/2pp1nq1/8/3Pp3/2N1NP2/PPP1B1PP/R1BQ1RK1 b kq - 0 10"));
		assertEquals("rnb1k2r/pp2b1pp/2pp1nq1/8/3Pp3/2N1NP2/PPP1B1PP/R1BQ1RK1 b kq - 0 10", b.convertToFEN());

		assertDoesNotThrow(() -> c = new Board("r1bq1r2/pp2n3/4N2k/3pPppP/1b1n2Q1/2N5/PP3PP1/R1B1K2R w KQ g6 0 15"));
		assertEquals("r1bq1r2/pp2n3/4N2k/3pPppP/1b1n2Q1/2N5/PP3PP1/R1B1K2R w KQ g6 0 15", c.convertToFEN());

		assertDoesNotThrow(() -> d = new Board("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2 10"));
		assertEquals("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2 10", d.convertToFEN());
	}

	@Test
	void testBadFENs() {
		assertThrows(FENException.class, () -> badFEN = new Board(""));
		assertThrows(FENException.class, () -> badFEN = new Board(" "));
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

    }
    @Test
    void testGoodFEN() {
		assertDoesNotThrow(() -> d = new Board("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq -"));
		assertEquals(0, d.getFiftyMoveRule());
		assertEquals(1, d.getFullMoveCount());

		assertDoesNotThrow(() -> d = new Board("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2"));
		assertEquals(2, d.getFiftyMoveRule());
		assertEquals(1, d.getFullMoveCount());
		assertEquals("r2qk2r/pppbbppp/2n5/1B1p4/3P4/5N2/P1P2PPP/R1BQR1K1 b kq - 2 1", d.convertToFEN());
	}

	@Test
	void testConvertToFEN() {
		start = new Board();
		assertEquals(0, start.getFiftyMoveRule());
		assertEquals(1, start.getFullMoveCount());
		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", start.convertToFEN());
	}

}
