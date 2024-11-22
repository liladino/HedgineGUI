package graphics.panels;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import chess.IO.PGNConverter;
import game.GameManager;
import game.interfaces.VisualChangeListener;
import utility.Sides;

public class RightPanel extends JPanel implements VisualChangeListener{
	private static final long serialVersionUID = 3474454907485110512L;
	private JTextArea whiteName;
	private JTextArea blackName;
	private JTextArea movesArea;
	private transient GameManager gameManager;
	
	public RightPanel(GameManager gameManager){
		setPreferredSize(new Dimension(300, getHeight()));

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 20, 10);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 0.05;
		gbc.weightx = 0.5;
		gbc.gridx = 0;
		gbc.gridy = 0;
		whiteName = new JTextArea("White Player");
		whiteName.setEditable(false);
		whiteName.setLineWrap(true);
		Font font = new Font("Courier new", Font.BOLD, 17);
        whiteName.setFont(font);
		
		add(whiteName, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		blackName = new JTextArea("Black Player");
		blackName.setEditable(false);
		blackName.setColumns(5);
		blackName.setLineWrap(true);
		blackName.setFont(font);
		add(blackName, gbc);
	
		gbc.weighty = 0.05;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		TimePanel whiteClockPanel = new TimePanel(Sides.WHITE);
		add(whiteClockPanel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		TimePanel blackClockPanel = new TimePanel(Sides.BLACK);
		add(blackClockPanel, gbc);
		
		this.gameManager = gameManager;
		gameManager.setClockPanels(whiteClockPanel, blackClockPanel);
		gameManager.addVisualChangeListener(this);
		
		movesArea = new JTextArea();
		movesArea.setFont(new Font("Courier new", Font.PLAIN, 16));
		movesArea.setLineWrap(true);
		movesArea.setWrapStyleWord(true);
		movesArea.setEditable(false);
		JScrollPane movesScrollPane = new JScrollPane(movesArea);
 
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;  // Span both columns
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(movesScrollPane, gbc);
	}

	public void setWhiteName(String name){
		whiteName.setText(name);
	}

	
	public void setBlackName(String name){
		blackName.setText(name);
	}

	@Override
	public void onGameStateChanged() {
		updateMoves();
	}

	@Override
	public void onGameLooksChanged() {
		updateMoves();
	}

	private void updateMoves(){
		String temp = PGNConverter.convertToMoves(gameManager.startFEN(), gameManager.getMoves());
		if (temp != null) movesArea.setText(temp);
	} 

}
