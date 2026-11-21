package hedgineGUI.game;

import java.util.Objects;
import java.util.List;
import hedgineGUI.core.chess.Move;

import hedgineGUI.core.IO.FENException;
import hedgineGUI.core.chess.Board;
import hedgineGUI.game.clock.Clock;
import hedgineGUI.game.clock.ClockBuilder;
import hedgineGUI.game.clock.ClockController;
import hedgineGUI.game.clock.Ticker;
import hedgineGUI.game.clock.TimeInputException;
import hedgineGUI.utility.Sides;

/** Immutable dependencies and starting values for one game. */
public final class GameConfiguration {
    public static final String STANDARD_START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final String fen;
    private final Player white;
    private final Player black;
    private final String timeControl;
    private final Ticker ticker;
    private final int searchDepth;
    private final List<Move> initialMoves;

    public GameConfiguration(
            String fen,
            Player white,
            Player black,
            String timeControl,
            Ticker ticker) {
        this(fen, white, black, timeControl, ticker, 0, List.of());
    }

    public GameConfiguration(String fen, Player white, Player black, String timeControl,
            Ticker ticker, int searchDepth, List<Move> initialMoves) {
        if (searchDepth < 0) throw new IllegalArgumentException("Search depth cannot be negative");
        this.searchDepth = searchDepth;
        this.initialMoves = initialMoves.stream()
                .map(move -> move.withEvaluation(null)).toList();
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

    public int getSearchDepth() { return searchDepth; }

    public List<Move> getInitialMoves() {
        return initialMoves.stream().map(Move::new).toList();
    }

    public GameConfiguration withOpening(String position, List<Move> moves) {
        return new GameConfiguration(position, white, black, timeControl, ticker, searchDepth, moves);
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
