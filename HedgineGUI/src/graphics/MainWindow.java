package graphics;

import game.GameManager;

import java.awt.BorderLayout;

import javax.swing.*;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 38435486L;
	private ChessBoardPanel chessBoardPanel;
    //private GameManager gameManager;

    public MainWindow(GameManager gameManager) {
        //this.gameManager = gameManager;
        setTitle("Chess Game");
        setSize(600 + getInsets().left + getInsets().right, 600 + getInsets().top + getInsets().bottom);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		chessBoardPanel = new ChessBoardPanel(gameManager, 600);
		
		setLayout(new BorderLayout());
		add(chessBoardPanel, BorderLayout.CENTER);
		pack();
		
		//add(chessBoardPanel);
		chessBoardPanel.repaint();
        
        setVisible(true);
    }
}