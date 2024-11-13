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

public class NewGame extends JFrame {
	private JComboBox<String> comboWhitePlayer;
	private JTextField whiteEnginePath;
	private JComboBox<String> comboBlackPlayer;
	private JTextField blackEnginePath; 
	ButtonGroup timeControlSelectionGroup;

	public NewGame(){
		initialzeWindow();
		initialze();
		pack();
	}

	void initialzeWindow(){
		setTitle("New game");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
		
		setLayout(new GridBagLayout());
	}

	void initialze(){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 10, 5, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		add(new JLabel("Players:"), gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		add(new JLabel("White:"), gbc);

		//TODO: add name field for player
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
		add(new JLabel("Black:"), gbc);

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

		
		timeControlSelectionGroup = new ButtonGroup();
		JRadioButton radioNoControl = new JRadioButton("No time control");
		radioNoControl.setSelected(true);
		JRadioButton radioFischer = new JRadioButton("Fischer");
		JRadioButton radioFixTime = new JRadioButton("Fix time per move");
		timeControlSelectionGroup.add(radioNoControl);
		timeControlSelectionGroup.add(radioFischer);
		timeControlSelectionGroup.add(radioFixTime);
		
		String[] presets = {"No preset", "Bullet 1+0", "Blitz 3+2", "Blitz 5+0", "Rapid 10+10", "Tournament", "WCC"};
		JComboBox<String> fischerPresets = new JComboBox<>(presets);
		fischerPresets.setSelectedIndex(1);
		fischerPresets.setVisible(false);

		JTextField fischerControl = new JTextField(10);
		fischerControl.setVisible(false);
		JTextField fixTimeControl = new JTextField(5);
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
			
		}
	} 


}
