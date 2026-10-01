package game;

/** Why a game stopped; independent from who won. */
public enum GameTermination {
    NONE,
    CHECKMATE,
    STALEMATE,
    DRAW,
    RESIGNATION,
    TIMEOUT,
    ABORTED,
    ERROR
}
