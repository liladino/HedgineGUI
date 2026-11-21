package hedgineGUI.core.chess;

/** An exact engine evaluation in centipawns from White's perspective. */
public record MoveEvaluation(long centipawns, int depth) {
    public MoveEvaluation {
        if (depth < 0) throw new IllegalArgumentException("Depth must be nonnegative");
    }

    public String pgnComment() {
        return "{" + centipawns + "/" + depth + "}";
    }
}
