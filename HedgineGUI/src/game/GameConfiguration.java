package game;

import java.util.Objects;

import core.chess.Board;
import core.chess.IO.FENException;
import game.clock.Clock;
import game.clock.ClockBuilder;
import game.clock.ClockController;
import game.clock.Ticker;
import game.clock.TimeInputException;
import utility.Sides;

/** Immutable dependencies and starting values for one game. */
public final class GameConfiguration {
    public static final String STANDARD_START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final String fen;
    private final Player white;
    private final Player black;
    private final String timeControl;
    private final Ticker ticker;

    public GameConfiguration(
            String fen,
            Player white,
            Player black,
            String timeControl,
            Ticker ticker) {
        this.fen = normalizeFen(fen);
        this.white = Objects.requireNonNull(white, "white");
        this.black = Objects.requireNonNull(black, "black");
        this.timeControl = Objects.requireNonNull(timeControl, "timeControl");
        this.ticker = Objects.requireNonNull(ticker, "ticker");

        if (white.getSide() != Sides.WHITE || black.getSide() != Sides.BLACK) {
            throw new IllegalArgumentException("Players must match their configured sides");
        }
        if (white == black) {
            throw new IllegalArgumentException("White and black must be distinct players");
        }
    }

    private static String normalizeFen(String fen) {
        if (fen == null || fen.trim().isEmpty() || "startpos".equals(fen.trim())) {
            return STANDARD_START_FEN;
        }
        return fen.trim();
    }

    Board createBoard() throws FENException {
        return new Board(fen);
    }

    ClockController createClockController() throws TimeInputException {
        Clock clock = new Clock();
        ClockBuilder.setClock(clock, timeControl);
        return new ClockController(ticker, clock);
    }

    public String getFen() {
        return fen;
    }

    public Player getWhite() {
        return white;
    }

    public Player getBlack() {
        return black;
    }
}
