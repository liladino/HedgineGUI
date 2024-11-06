package game;

import java.util.ArrayList;

import utility.*;

public class Clock implements Runnable {
	private TimeControl controlType;
	private int whiteTime; 
	private int blackTime;
	private int increment; 
	private int incrementStartMove; //e.g. increment comes after the 40th move
	private int moveTime; 
	private Sides activePlayer;
	ArrayList<Pair<Integer, Integer>> extraTimes; //after move X give players Y time (moveCount, extraTime)

    public Clock(){
        controlType = TimeControl.NO_CONTROL;
		extraTimes = new ArrayList<>();
		activePlayer = Sides.WHITE;
    }


    @Override
    public void run() {
        
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

	public void pressClock(){
		if (activePlayer == Sides.WHITE) {
			activePlayer = Sides.BLACK;
			if (controlType == TimeControl.FIX_TIME_PER_MOVE){
				blackTime = moveTime;
			}
		}
		else {
			activePlayer = Sides.WHITE;
			if (controlType == TimeControl.FIX_TIME_PER_MOVE){
				whiteTime = moveTime;
			}
		}

	}
    
}
