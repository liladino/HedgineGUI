package graphics.dialogs;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import utility.Sides;

public class GameEndDialogs {
	private GameEndDialogs(){}
	public static void showCheckmate(JFrame parent, Sides won) {
		String message = "Ckeckmate! " + (won == Sides.WHITE ? "White" : "Black") + " won.";
		JOptionPane.showMessageDialog(parent, message, "Game over", JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showStalemate(JFrame parent) {
		String message = "Stalemate! The game is a draw." ;
		JOptionPane.showMessageDialog(parent, message, "Game over", JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showInsufficientMaterial(JFrame parent) {
		String message = "Insufficient material! The game is a draw." ;
		JOptionPane.showMessageDialog(parent, message, "Game over", JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showDraw(JFrame parent) {
		String message = "Draw." ;
		JOptionPane.showMessageDialog(parent, message, "Game over", JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showWonOnTime(JFrame parent, Sides won){
		String message = (won == Sides.WHITE ? "White won on time." : "Black won on time.") ;
		JOptionPane.showMessageDialog(parent, message, "Game over", JOptionPane.INFORMATION_MESSAGE);
	}
	
}
