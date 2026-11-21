package hedgineGUI.game;

import hedgineGUI.core.chess.MoveEvaluation;
import hedgineGUI.utility.Sides;

/** Collects one search's deepest primary variation score, never mixing searches or depths. */
public final class UciSearchInfo {
    private int deepest = -1;
    private MoveEvaluation evaluation;

    public synchronized void reset() {
        deepest = -1;
        evaluation = null;
    }

    public synchronized void accept(String line, Sides side) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length < 2 || !tokens[0].equals("info") || tokens[1].equals("string")) return;
        Integer depth = null, cp = null;
        boolean mate = false, bound = false;
        int variation = 1;
        try {
            for (int i = 1; i < tokens.length; i++) {
                String token = tokens[i];
                if (token.equals("pv") || token.equals("string")) break;
                switch (token) {
                    case "depth" -> depth = Integer.valueOf(tokens[++i]);
                    case "multipv" -> variation = Integer.parseInt(tokens[++i]);
                    // Accept both standard "score cp X" and the engine's shorter "cp X".
                    case "cp" -> cp = Integer.valueOf(tokens[++i]);
                    case "mate" -> { Integer.parseInt(tokens[++i]); mate = true; }
                    case "lowerbound", "upperbound" -> bound = true;
                    default -> { }
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException malformed) {
            return;
        }
        if (variation != 1 || depth == null || depth < 0 || (cp == null && !mate)) return;
        if (depth >= deepest) {
            deepest = depth;
            evaluation = cp == null || mate || bound ? null
                    : new MoveEvaluation(side == Sides.WHITE ? (long) cp : -(long) cp, depth);
        }
    }

    public synchronized MoveEvaluation finish() {
        MoveEvaluation result = evaluation;
        reset();
        return result;
    }
}
