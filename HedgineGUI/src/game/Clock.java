package game;

import java.util.ArrayList;

import utility.*;

public class Clock implements Runnable, GameEventListener {
	private TimeControl controlType;
	private int whiteTime; 
	private int blackTime;
	private int increment; 
	private int incrementStartMove; //e.g. increment comes after the 40th move

	//TODO the starting player and plies should be from the fen
	private int plies; //how many times the clock was pressed
	private int moveTime; 
	private boolean isWhiteActive;
	private TimeEventListener timeEventListener;
	private ArrayList<Pair<Integer, Integer>> extraTimes; //after move X give players Y time (moveCount, extraTime)

	private volatile boolean clockWasPressed;
	private volatile boolean timerUp; // signal for time expiration
    private volatile boolean ticking; // tracks if the timer should be ticking

    public Clock(TimeControl controlType, GameManager gameManager){
        this.controlType = controlType;
		extraTimes = new ArrayList<>();
		isWhiteActive = true;
		plies = 0;
		gameManager.addGameEventListener(this);
    }

	/* * * * * *
	 * SETTERS *
	 * * * * * */
	public void setStartTime(int startTime){
		whiteTime = blackTime = startTime;
	}
	public void setMoveTime(int moveTime){
		this.moveTime = whiteTime = blackTime = moveTime;
	}
	public void setIncrement(int increment, int incrementStartMove){
		this.increment = increment;
		this.incrementStartMove = incrementStartMove;
	}
	public void addExtraTime(int afterMoveX, int addTimeY){
		extraTimes.add(new Pair<>(afterMoveX, addTimeY));
	}
	public void setTimeEventListener(TimeEventListener timeEventListener){
		this.timeEventListener = timeEventListener;
	}

	/* * * * * *
	 * GETTERS *
	 * * * * * */
	public TimeControl getTimeControl(){
		return controlType;
	}

	public Sides activeSide(){
		if (isWhiteActive) return Sides.WHITE;
		return Sides.BLACK;
	}

	/* * * * * * * *
	 * MAIN METHOD *
	 * * * * * * * */

    @Override
    public void run() {
		long startTime = System.currentTimeMillis();
        while (!timerUp) {
			int tikRateMillis = 500;
            synchronized (this){
				clockWasPressed = false;
				try {
					wait(tikRateMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			long elapsed = System.currentTimeMillis() - startTime;
			
			if (ticking) {
				if (isWhiteActive) {
					whiteTime -= (int)elapsed;
					if (whiteTime <= 0) {
						timerUp = true;
						signalTimeIsUp();
					}
				} else {
					blackTime -= (int)elapsed;
					if (blackTime <= 0) {
						timerUp = true;
						signalTimeIsUp();
					}
				}
				
				if (clockWasPressed){
					updateClockData();
				}
                updateDisplay();
            }
			startTime = System.currentTimeMillis();
        }
    }

	private void signalTimeIsUp() {
		timeEventListener.onTimeIsUp();
    }

	public synchronized void pressClock(){
		clockWasPressed = true;
		notifyAll();
	}

	public void updateClockData(){
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

	private void updateDisplay(){
		System.out.printf("Time remaining: white: %d, black: %d%n", whiteTime, blackTime);
	}

	/* * * * * * * * * * *
	 * INTERFACE METHODS *
	 * * * * * * * * * * */
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
