package graphics;

import game.GameManager;
import utility.Sides;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.*;

public class MainWindow extends JFrame implements GameEventListener {
	private static final long serialVersionUID = 38435486L;
	private ChessBoardPanel chessBoardPanel;
	private MenuManager menuManager;
	
	public MainWindow(GameManager gameManager) {
		setTitle("Chess Game");
		setMinimumSize(new Dimension(240, 280));
		setSize(600 + getInsets().left + getInsets().right, 600 + getInsets().top + getInsets().bottom);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameManager.setGameEventListener(this);
		
		chessBoardPanel = new ChessBoardPanel(gameManager, 600);
		
		setLayout(new BorderLayout());
		add(chessBoardPanel, BorderLayout.CENTER);
		pack();
		
		menuManager = new MenuManager(this);
		
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
}