package hedgineGUI.tournament;

import hedgineGUI.control.GameController;

/** Two games between the same players from the same opening, played once in each color. */
public final class GamePair {
    private final String player;
    private final String opponent;
    private final GameController[] legs = new GameController[2];

    /** The player has White in the first leg. */
    public GamePair(String player, String opponent) {
        this.player = player;
        this.opponent = opponent;
    }

    public String player() { return player; }
    public String opponent() { return opponent; }

    /** The latest attempt of the leg; a replay replaces its timed-out predecessor. */
    public GameController leg(int leg) { return legs[leg]; }
    public void setLeg(int leg, GameController game) { legs[leg] = game; }
}
