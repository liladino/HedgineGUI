package hedgineGUI.game.clock;

import hedgineGUI.utility.Second;
import hedgineGUI.utility.TimeControl;

import java.math.BigDecimal;


/**
 * Helps setting a Clock object / or its values based on the time info string.
 * Works in both ways:
 *	if instanced, can build a valid time information string step by step
 *	with the static setClock method can set a Clock from a string
 */
public class ClockBuilder {
	private StringBuilder time;
	public ClockBuilder(){
		time = new StringBuilder();
		time.append('N');
	}

	public void setNoControl(){
		time = new StringBuilder();
		time.append('N');
	}

	public void setFixTime(Second seconds){
		time = new StringBuilder();
		time.append("X ");
		time.append(seconds);
	}

	public void setFischer(Second startTime, int incrementStartMove, Second increment){
		time = new StringBuilder();
		time.append("S ");
		time.append(startTime);
		time.append(' ');
		
		time.append(incrementStartMove);
		time.append(' ');
		
		time.append(increment);
		time.append(' ');
	}

	public void addExtraTime(int atMove, Second extraTime){
		time.append(atMove);
		time.append(' ');

		time.append(extraTime);
		time.append(' ');
	}

	public String getTimeInformation(){
		return new String(time);
	}

	public void setClock(Clock c) throws TimeInputException{
		setClock(c, getTimeInformation());
	}

    /**
     * Durations accept seconds (1, 1.5) or whole milliseconds (ms500).
     * Values must be nonnegative, exact milliseconds and fit in a signed int.
     * Move indices always remain nonnegative integers.
     */
    private static int durationMs(String token) throws TimeInputException {
        try {
            if (token.matches("ms[0-9]+")) return Integer.parseInt(token.substring(2));
            if (!token.matches("[0-9]+(\\.[0-9]+)?"))
                throw new NumberFormatException();
            return new BigDecimal(token).movePointRight(3).intValueExact();
        } catch (ArithmeticException | NumberFormatException error) {
            throw new TimeInputException("Invalid duration: " + token
                    + " (use seconds such as 1.5, or milliseconds such as ms500)");
        }
    }

    private static int moveNumber(String token) throws TimeInputException {
        try {
            if (!token.matches("[0-9]+")) throw new NumberFormatException();
            return Integer.parseInt(token);
        } catch (NumberFormatException error) {
            throw new TimeInputException("Invalid move number: " + token);
        }
    }

    public static void setClock(Clock c, String timeInformation) throws TimeInputException {
        if (timeInformation == null || timeInformation.isBlank())
            throw new TimeInputException("No time information");
        String[] tokens = timeInformation.trim().split("\\s+");
        String mode = tokens[0];
        if (!mode.equals("N") && !mode.equals("X") && !mode.equals("S"))
            throw new TimeInputException("Unknown format specifier (valid: N, X, S)");
        if (!mode.equals("N") && tokens.length < 2)
            throw new TimeInputException("No start time specified");

        // Validate before modifying the clock. Keep accepting legacy trailing tokens.
        int[] values = new int[tokens.length];
        for (int i = 1; i < tokens.length; i++)
            values[i] = mode.equals("S") && i >= 2 && i % 2 == 0
                    ? moveNumber(tokens[i]) : durationMs(tokens[i]);
        if (mode.equals("N")) {
            c.setControlType(TimeControl.NO_CONTROL);
        } else if (mode.equals("X")) {
            c.setControlType(TimeControl.FIX_TIME_PER_MOVE);
            c.setMoveTimeMs(values[1]);
        } else {
            c.setControlType(TimeControl.FISCHER);
            c.setStartTimeMs(values[1]);
            c.setIncrementMs(tokens.length >= 4 ? values[3] : 0,
                    tokens.length >= 4 ? values[2] : 0);
            for (int i = 4; i + 1 < tokens.length; i += 2)
                c.addExtraTimeMs(values[i], values[i + 1]);
        }
    }
}
