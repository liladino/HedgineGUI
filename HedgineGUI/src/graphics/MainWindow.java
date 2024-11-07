package graphics;

import game.GameEventListener;
import game.GameManager;
import game.TimeEventListener;
import graphics.dialogs.GameEndDialogs;
import graphics.panels.ChessBoardPanel;
import graphics.panels.RightPanel;
import utility.Sides;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;

public class MainWindow extends JFrame implements GameEventListener {
	private static final long serialVersionUID = 38435486L;
	private ChessBoardPanel chessBoardPanel;
	private RightPanel rightPanel;
	private transient MenuManager menuManager;
	
	public MainWindow(GameManager gameManager) {
		setTitle("Chess");
		setMinimumSize(new Dimension(240, 280));
		setSize(830 + getInsets().left + getInsets().right, 600 + getInsets().top + getInsets().bottom);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		gameManager.addGameEventListener(this);
		
		GraphicSettings.initializeGraphicSettings();
		menuManager = new MenuManager(this);
		chessBoardPanel = new ChessBoardPanel(gameManager, 100, menuManager);
		rightPanel = new RightPanel();
		
		setLayout(new BorderLayout());
		
		add(chessBoardPanel, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.EAST);
		//pack();
		
		chessBoardPanel.repaint();
		
		setVisible(true);
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
}