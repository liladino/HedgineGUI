package game;

import utility.Second;
import utility.TimeControl;

public class TimeInformationConverter {
	private StringBuilder time;
	public TimeInformationConverter(){
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

	public static void setClock(Clock c, String timeInformation) throws TimeInputException{
		String[] tokens = timeInformation.split(" ");
		if (tokens.length < 1){
			throw new TimeInputException("No time information");
		}
		if (!tokens[0].equals("N") && tokens.length < 2){
			throw new TimeInputException("No starttime specified");
		}
		
		for (int i = 1; i < tokens.length; i++){
			if (!tokens[i].matches("^[0-9 ]*$")) {
				throw new TimeInputException("Invalid String format");
			}
		}

		//szeszkultura zh ket het mulva 
		
		if (tokens[0].equals("N")){
			c.setControlType(TimeControl.NO_CONTROL);
		}
		else if (tokens[0].equals("X")) {
			c.setControlType(TimeControl.FIX_TIME_PER_MOVE);
			
			int seconds = Integer.parseInt(tokens[1]);
			c.setMoveTime(new Second(seconds));
		}
		else if (tokens[0].equals("S")){
			c.setControlType(TimeControl.FISCHER);
			
			int startTime = Integer.parseInt(tokens[1]);

			c.setStartTime(new Second(startTime));

			if (tokens.length >= 4){
				int incrementStartMove = Integer.parseInt(tokens[2]);
				int increment = Integer.parseInt(tokens[3]);

				c.setIncrement(new Second(increment), incrementStartMove);
			}
			else {
				c.setIncrement(new Second(0), 0);
			}

			for (int len = 4; len+1 < tokens.length; len += 2){
				int atMove = Integer.parseInt(tokens[len]);
				int extraTime = Integer.parseInt(tokens[len+1]);

				c.addExtraTime(atMove, new Second(extraTime));
			}

		}
		else {
			throw new TimeInputException("Unknown format specifier (valid: N - no control, X - fix time per move, S - Fischer)");
		}
	}
}
