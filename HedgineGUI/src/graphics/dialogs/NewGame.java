package graphics.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import game.GameStarter;
import game.Human;
import game.Player;
import utility.Sides;

public class NewGame extends JFrame {
	private JTextField whiteName;
	private JTextField blackName;
	private JComboBox<String> comboWhitePlayer;
	private JTextField whiteEnginePath;
	private JComboBox<String> comboBlackPlayer;
	private JTextField blackEnginePath; 
	private JTextField startPos;
	private String timeControl;
	private JTextField fischerControl;
	private JTextField fixTimeControl;
	private JComboBox<String> fischerPresets;

	public NewGame(){
		initialzeWindow();
		initialze();
		//pack();
	}

	void initialzeWindow(){
		setTitle("New game");
		setSize(600, 600);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
		
		setLayout(new GridBagLayout());
	}

	void initialze(){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		/* * * * * *
		 * Players *
		 * * * * * */
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(new JLabel("Players:"), gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		whiteName = new JTextField("White");
		add(whiteName, gbc);

		gbc.gridx = 1;
		String[] playertypes = {"Human", "Engine"};
		comboWhitePlayer = new JComboBox<>(playertypes);
		add(comboWhitePlayer, gbc);

		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy++;
		whiteEnginePath = new JTextField(20);
		add(whiteEnginePath, gbc);
		whiteEnginePath.setVisible(false);

		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		blackName = new JTextField("Black");
		add(blackName, gbc);

		gbc.gridx = 1;
		comboBlackPlayer = new JComboBox<>(playertypes);
		add(comboBlackPlayer, gbc);

		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy++;
		blackEnginePath = new JTextField(20);
		add(blackEnginePath, gbc);
		blackEnginePath.setVisible(false);

		/* * * * * * *
		 * Start fen *
		 * * * * * * */
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		add(new JLabel("FEN:"), gbc);
		startPos = new JTextField("startpos", 20);		
		gbc.gridx = 1;
		add(startPos, gbc);

		/* * * * * * * * *
		 * Time control  *
		 * * * * * * * * */
		ButtonGroup timeControlSelectionGroup = new ButtonGroup();
		
		JRadioButton radioFischer = new JRadioButton("Fischer");
		JRadioButton radioFixTime = new JRadioButton("Fix time per move");
		JRadioButton radioNoControl = new JRadioButton("No time control");
		radioNoControl.setSelected(true);
		timeControl = "N";

		timeControlSelectionGroup.add(radioNoControl);
		timeControlSelectionGroup.add(radioFischer);
		timeControlSelectionGroup.add(radioFixTime);

		radioFixTime.addActionListener(new FixTimeActionListener());
		radioNoControl.addActionListener(new NoControlActionListener());
		radioFischer.addActionListener(new FischerActionListener());
		
		String[] presets = {"No preset", "Bullet 1+0", "Blitz 3+2", "Blitz 5+0", "Rapid 10+10", "Tournament", "WCC"};
		fischerPresets = new JComboBox<>(presets);
		fischerPresets.setSelectedIndex(2);
		fischerPresets.setVisible(false);

		fischerControl = new JTextField();
		fischerControl.setVisible(false);

		fixTimeControl = new JTextField("X 60");
		fixTimeControl.setVisible(false);
		
		gbc.weighty = 0.05;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		add(radioNoControl, gbc);
		gbc.gridy++;
		add(radioFischer, gbc);

		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		add(fischerPresets, gbc);
		gbc.gridx = 1;
		add(fischerControl, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		add(radioFixTime, gbc);
		gbc.gridx = 1;
		add(fixTimeControl, gbc);

		/* * * * * * * * *
		 * start button  *
		 * * * * * * * * */
		JButton startGame = new JButton("Start");
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy++;
		add(startGame, gbc);

		startGame.addActionListener(new StartGameAction());
	}

	private class StartGameAction implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			Player w = null;
			Player b = null;
			if (comboWhitePlayer.getSelectedItem().equals("Human")){
				w = new Human(Sides.WHITE, whiteName.getText());
			}
			if (comboBlackPlayer.getSelectedItem().equals("Human")){
				b = new Human(Sides.BLACK, blackName.getText());
			}

			GameStarter.startNewGame(
				(startPos.getText().equals("startpos") || startPos.getText().equals("") 
					? "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" : startPos.getText()), 
				w, b, timeControl);
			
			dispose();
		}
	} 

	private class FixTimeActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			fixTimeControl.setVisible(true);

			fischerPresets.setVisible(false);
			fischerControl.setVisible(false);
		}
	}

	private class FischerActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			fixTimeControl.setVisible(false);

			fischerPresets.setVisible(true);
			fischerControl.setVisible(true);
		}
	}

	private class NoControlActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			timeControl = "N";
			fixTimeControl.setVisible(false);
			fischerPresets.setVisible(false);
			fischerControl.setVisible(false);
		}
	}
}
