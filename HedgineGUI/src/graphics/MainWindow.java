package graphics;

import game.GameManager;
import utility.Sides;

import java.awt.BorderLayout;

import javax.swing.*;

public class MainWindow extends JFrame implements GameEventListener {
    private static final long serialVersionUID = 38435486L;
	private ChessBoardPanel chessBoardPanel;

    public MainWindow(GameManager gameManager) {
        setTitle("Chess Game");
        setSize(600 + getInsets().left + getInsets().right, 600 + getInsets().top + getInsets().bottom);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameManager.setGameEventListener(this);
        
		chessBoardPanel = new ChessBoardPanel(gameManager, 600);
		
		setLayout(new BorderLayout());
		add(chessBoardPanel, BorderLayout.CENTER);
		pack();
		
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