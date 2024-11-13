package graphics.dialogs;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class InformationDialogs {
	private InformationDialogs(){}
	
	public static void errorDialog(JFrame parent, String message){
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.INFORMATION_MESSAGE);
	}
	
}
