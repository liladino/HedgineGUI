package graphics.mainWindow;

import game.GameManager;
import javax.swing.*;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 38435486L;
	//private ChessBoardPanel chessBoardPanel;
    private GameManager gameManager;

    public MainWindow() {
     //   this.gameManager = gameManager;
        setTitle("Chess Game");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize the chessboard panel and pass the GameManager for communication
        //chessBoardPanel = new ChessBoardPanel(gameManager);
        //add(chessBoardPanel);

        setVisible(true);
    }
}