package game;

import java.util.ArrayList;

import utility.*;

public class Clock implements Runnable, GameEventListener {
	private TimeControl controlType;
	private int whiteTime; 
	private int blackTime;
	private int increment; 
	private int incrementStartMove; //e.g. increment comes after the 40th move
	private int plies; //how many times the clock was pressed
	private int moveTime; 
	private boolean isWhiteActive;
	private TimeEventListener timeEventListener;
	private ArrayList<Pair<Integer, Integer>> extraTimes; //after move X give players Y time (moveCount, extraTime)

	private volatile boolean timerUp; // signal for time expiration
    private volatile boolean ticking; // tracks if the timer should be ticking

    public Clock(TimeControl controlType){
        this.controlType = controlType;
		extraTimes = new ArrayList<>();
		isWhiteActive = true;
		plies = 0;
    }

	public Sides activePlayer(){
		if (isWhiteActive) return Sides.WHITE;
		return Sides.BLACK;
	}

	public void setTimeEventListener(TimeEventListener timeEventListener){
		this.timeEventListener = timeEventListener;
	}


    @Override
    public void run() {
        while (!timerUp) {
			int tikRateMillis = 500;
            try {
                Thread.sleep(tikRateMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
			if (ticking) {
                if (isWhiteActive) {
                    whiteTime -= tikRateMillis;
                    if (whiteTime <= 0) {
                        timerUp = true;
                        notifyTimeIsUp();
                    }
                } else {
                    blackTime -= tikRateMillis;
                    if (blackTime <= 0) {
                        timerUp = true;
						notifyTimeIsUp();
                    }
                }
                //updateDisplay(); // method to update GUI timer display
            }
        }
    }

	private void notifyTimeIsUp() {
		timeEventListener.onTimeIsUp();
    }

	public void pressClock(){
		plies++;
		if (controlType == TimeControl.FIX_TIME_PER_MOVE){
			blackTime = whiteTime = moveTime;
		}
		else if (controlType == TimeControl.FISCHER){
			if (plies/2 > incrementStartMove){
				if (isWhiteActive) whiteTime += increment;
				else blackTime += increment;
			}
			for (Pair<Integer, Integer> p : extraTimes){
				if (plies/2 == p.first){
					if (isWhiteActive) whiteTime += p.second;
					else blackTime += p.second;
				}
			}
		}
		isWhiteActive = !isWhiteActive;
	}

	public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }

	@Override
	public void onCheckmate(Sides won) {
		ticking = false;
	}

	@Override
	public void onDraw() {
		ticking = false;
	}

	@Override
	public void onStalemate() {
		ticking = false;
	}

	@Override
	public void onInsufficientMaterial() {
		ticking = false;
	}

	@Override
	public void onTimeIsUp(Sides won) {
		ticking = false;
	}

	@Override
	public void onTimeIsUp() {
		ticking = false;
	}    
}
