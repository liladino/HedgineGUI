package graphics.dialogs;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Shows custom information/error messages, as well as the infos about the program.
 */
public class InformationDialogs {
	private InformationDialogs(){}
	
	public static void errorDialog(JFrame parent, String message){
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void infoDialog(JFrame parent, String message){
		JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void aboutDialog(JFrame parent) {
		JPanel logoPanel = new JPanel();
		JTextArea label = new JTextArea("                                     /    #\n# .  .     .                       #   *.  # #   ##  .#\n# |__| _  _| _ . _  _       # (  #       #.   #,     /,##,     #(\n# |  |(/,(_](_]|[ )(/,  (  #   ##                             # .,\n#           ._|        # #.,                                      #\n#                     #   #                                              /#\n#                  ./##                           ||                  *#\n#                   #                            ====                     ##\n#                   #                       ____  ||  ____                  #\n#                  +.    &&                / __ \\ /\\ / __ \\                /#\n#                 &   &&     &&           | /  \\ |  | /  \\ |               #\n#     &&*       &    *&& #      &         | \\   \\ \\/ /   / |                ,.\n#   &    &#+&&     .#    &       &         \\ \\__/ || \\__/ /                (,\n#   &     &         &  & &        &        |______________|                #*#\n#    &&&&.           /&&          &         \\____________/                #\n#        &                       /.                                 (###/  #\n#         &        # &           &                                   (\n#           *&      &           &                        .##    #   ###\n#               *&&          *&#  ,##   #    ((##( /   ##  #  #*#\n#                                     (#    ###     \n#");
		label.setFont(new Font("Courier new", Font.PLAIN, 10));
		label.setSize(new Dimension(500, 500));
		label.setEditable(false);
		label.setLineWrap(true);
		logoPanel.add(label);
		JOptionPane.showMessageDialog(parent, logoPanel, "About", JOptionPane.INFORMATION_MESSAGE);
	}
}
