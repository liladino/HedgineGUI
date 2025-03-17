package graphics;

import game.GameManager;
import game.GameStarter;
import game.interfaces.GameEventListener;
import graphics.dialogs.GameEndDialogs;
import graphics.panels.ChessBoardPanel;
import graphics.panels.RightPanel;
import utility.Sides;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

/**
 * The main window of the application. 
 * Implements GameEventListener, and shows the appropiate message upon the end of the game
 */
public class MainWindow extends JFrame implements GameEventListener {
	private static final long serialVersionUID = 38435486L;
	private ChessBoardPanel chessBoardPanel;
	private RightPanel rightPanel;
	private transient MenuManager menuManager;
	
	public MainWindow(GameManager gameManager) {
		setTitle("Chess");
		setMinimumSize(new Dimension(600 + getInsets().left + getInsets().right, 400 + getInsets().top + getInsets().bottom));
		setSize(900 + getInsets().left + getInsets().right, 640 + getInsets().top + getInsets().bottom);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (GameStarter.getGameManager() != null) GameStarter.getGameManager().stopRunning();
                try { Thread.sleep(100); } catch (InterruptedException exc) {}
    			System.exit(0);
            }
        });

		gameManager.addGameEventListener(this);
		
		GraphicSettings.initializeGraphicSettings();
		menuManager = new MenuManager(this);
		chessBoardPanel = new ChessBoardPanel(gameManager, menuManager);
		rightPanel = new RightPanel(gameManager);
		
		setLayout(new BorderLayout());
		menuManager.addGameEventListener(this);
		menuManager.addGameEventListener(gameManager.getClock());
		
		add(chessBoardPanel, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.EAST);
		pack();
		
		chessBoardPanel.repaint();
		
		setVisible(true);
	}

	public RightPanel getRightPanel(){
		return rightPanel;
	}
	
	@Override
	public void onCheckmate(Sides won) {
		GameEndDialogs.showCheckmate(this, won);
	}
	@Override
	public void onDraw() {
		GameEndDialogs.showDraw(this);
	}
	@Override
	public void onStalemate() {
		GameEndDialogs.showStalemate(this);
	}
	@Override
	public void onInsufficientMaterial() {
		GameEndDialogs.showInsufficientMaterial(this);
	}

	@Override
	public void onTimeIsUp(Sides won) {
		GameEndDialogs.showWonOnTime(this, won);
	}

	@Override
	public void onTimeIsUp() {
		GameEndDialogs.showDraw(this);
	}

	@Override
	public void onResign(Sides won) {
		GameEndDialogs.showResigned(this, won);
	}
}