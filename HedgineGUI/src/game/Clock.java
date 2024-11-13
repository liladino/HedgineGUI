package game;

import java.util.ArrayList;

import game.interfaces.ClockListener;
import game.interfaces.GameEventListener;
import game.interfaces.TimeEventListener;
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
	private ArrayList<Pair<Integer, Integer>> extraTimes; //after move X give players Y time (moveCount, extraTime)
	
	private TimeEventListener timeEventListener;
	private ClockListener whiteClockPanel;
	private ClockListener blackClockPanel;

	private volatile boolean clockWasPressed;
	private volatile boolean timerUp; // signal for time expiration
	private volatile boolean ticking; // tracks if the timer should be ticking
	private volatile boolean gameEnded;

	public Clock(TimeControl controlType, GameManager gameManager){
		this.controlType = controlType;
		extraTimes = new ArrayList<>();
		isWhiteActive = true;
		plies = 0;
		gameEnded = false;
		gameManager.addGameEventListener(this);
	}

	/* * * * * *
	 * SETTERS *
	 * * * * * */
	public void setStartTime(int startTime){
		whiteTime = blackTime = startTime;
		if (controlType == TimeControl.FIX_TIME_PER_MOVE) moveTime = startTime;
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
	public void setClockPanels(ClockListener whiteClockPanel, ClockListener blackClockPanel){
		this.whiteClockPanel = whiteClockPanel;
		this.blackClockPanel = blackClockPanel;

		if (controlType != TimeControl.NO_CONTROL){
			updateDisplay();
			whiteClockPanel.stoppedClock();
			blackClockPanel.stoppedClock();
		}
	}
	public void setPlyCount(int plies){
		this.plies = plies;
	}
	public void setActiveSide(Sides side){
		isWhiteActive = (side == Sides.WHITE);
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
		while (!timerUp && !gameEnded) {
			int tikRateMillis = 50;
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
						whiteTime = 0;
					}
				} else {
					blackTime -= (int)elapsed;
					if (blackTime <= 0) {
						timerUp = true;
						blackTime = 0;
					}
				}
				if (timerUp){
					updateDisplay();
					signalTimeIsUp();
				}
				else if (clockWasPressed){
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

	public synchronized void setTicking(boolean ticking) {
		this.ticking = ticking;
	}

	private void updateDisplay(){
		whiteClockPanel.updateClock((isWhiteActive ? Sides.WHITE : Sides.BLACK), whiteTime, blackTime);
		blackClockPanel.updateClock((isWhiteActive ? Sides.WHITE : Sides.BLACK), whiteTime, blackTime);
	}

	/* * * * * * * * * * *
	 * INTERFACE METHODS *
	 * * * * * * * * * * */
	private void onGameEnd(){
		ticking = false;
		gameEnded = true;
	}

	@Override
	public void onCheckmate(Sides won) {
		onGameEnd();
	}

	@Override
	public void onDraw() {
		onGameEnd();
	}

	@Override
	public void onStalemate() {
		onGameEnd();
	}

	@Override
	public void onInsufficientMaterial() {
		onGameEnd();
	}

	@Override
	public void onTimeIsUp(Sides won) {
		onGameEnd();
	}

	@Override
	public void onTimeIsUp() {
		onGameEnd();
	}	
}
