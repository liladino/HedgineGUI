package graphics.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class RightPanel extends JPanel{
    public RightPanel(){
        setPreferredSize(new Dimension(250, getHeight()));

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        
        JPanel whiteClockPanel = new JPanel();
        whiteClockPanel.add(new JLabel("White Clock")); // Replace with actual clock display
        add(whiteClockPanel, gbc);

        gbc.gridx = 1;
        JPanel whiteNamePanel = new JPanel();
        whiteNamePanel.add(new JLabel("White Player Name")); // Replace with actual name display
        add(whiteNamePanel, gbc);
        
        
        JTextArea movesArea = new JTextArea(10, 20);
        movesArea.setEditable(false);
        JScrollPane movesScrollPane = new JScrollPane(movesArea);
 
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;  // Span both columns
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(movesScrollPane, gbc);

        gbc.gridwidth = 1;  
        gbc.fill = GridBagConstraints.CENTER;
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        JPanel blackClockPanel = new JPanel();
        blackClockPanel.add(new JLabel("Black Clock")); // Replace with actual clock display
        add(blackClockPanel, gbc);

        gbc.gridx = 1;
        JPanel blackNamePanel = new JPanel();
        blackNamePanel.add(new JLabel("Black Player Name")); // Replace with actual name display
        add(blackNamePanel, gbc);
    }


}
