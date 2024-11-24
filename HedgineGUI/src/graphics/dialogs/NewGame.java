package graphics.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import game.Engine;
import game.GameStarter;
import game.Human;
import game.Player;
import utility.Sides;

public class NewGame extends JFrame {
	private static final long serialVersionUID = -421311633940977178L;
	private JTextField whiteName;
	private JTextField blackName;
	private JComboBox<String> comboWhitePlayer;
	private File whiteEngine;
	private JComboBox<String> comboBlackPlayer;
	private File blackEngine; 
	private JTextField startPos;
	private String timeControl;
	private JTextField fischerControl;
	private JTextField fixTimeControl;
	private JComboBox<String> fischerPresets;
	private JRadioButton radioFischer;
	private JRadioButton radioFixTime;
	private boolean updatingFromPreset = false;

	public NewGame(){
		this("startpos");
	}

	public NewGame(String fen){
		startPos = new JTextField(fen, 20);	
		initialzeWindow();
		initialze();
		pack();
		setVisible(true);
	}

	void initialzeWindow(){
		setTitle("New game");
		
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		setLayout(new GridBagLayout());
	}

	void initialze(){
  		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		
		whiteEngine = blackEngine = null;
		
		initRightPanel(gbc);

		initPlayerInfo(gbc);

		addSeparator(gbc);

		initFEN(gbc);

		addSeparator(gbc);

		initTimeSettings(gbc);
	}

	void initRightPanel(GridBagConstraints gbc){
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		try{
			gbc.gridx = 3;
			gbc.gridy = 0;
			gbc.gridheight = 13;
			gbc.fill = GridBagConstraints.BOTH;
			String imagesPath = System.getProperty("user.dir") + "/resources/menu/";
			ImageIcon menu = new ImageIcon(ImageIO.read(new File(imagesPath + "newgame1.png"))/*.getScaledInstance(224, 564, Image.SCALE_FAST)*/);
			
			JLabel picLabel = new JLabel(menu);
			add(picLabel, gbc);
		}
		catch (IOException i){
			//Image missing panel
			gbc.gridx = 3;
			gbc.gridy = 0;
			gbc.gridheight = 13;
			gbc.fill = GridBagConstraints.BOTH;
			JPanel imageSpace = new JPanel();
			imageSpace.add(new JLabel("Failed to load image"));
			imageSpace.setPreferredSize(new Dimension(224, 564));
			add(imageSpace, gbc);
		}
	}

	void initPlayerInfo(GridBagConstraints gbc){
		/* * * * * * * * *
		 * Player white  *
		 * * * * * * * * */
		gbc.gridheight = 1;	
		gbc.fill = GridBagConstraints.HORIZONTAL;
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

		/* * * * * * * * * * * *
		 * White engine picker *
		 * * * * * * * * * * * */
		gbc.gridx = 0;
		gbc.gridy++;
		JButton whiteEngineFromFile = new JButton("Choose File...");
		JLabel whiteEnginePath = new JLabel();
		whiteEngineFromFile.addActionListener(new EngineChooserButton(whiteName, whiteEnginePath, Sides.WHITE));

		add(whiteEngineFromFile, gbc);
		gbc.gridx = 1;
		add(whiteEnginePath, gbc);

		whiteEnginePath.setVisible(false);
		whiteEngineFromFile.setVisible(false);
		comboWhitePlayer.addActionListener(e -> {
			if (comboWhitePlayer.getSelectedItem().equals("Engine")){
				whiteEnginePath.setVisible(true);
				whiteEngineFromFile.setVisible(true);
			}
			else{
				whiteEnginePath.setVisible(false);
				whiteEngineFromFile.setVisible(false);
			}
			NewGame.this.revalidate();
			NewGame.this.repaint();
		});

		/* * * * * * * * *
		 * Player black  *
		 * * * * * * * * */
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		blackName = new JTextField("Black");
		add(blackName, gbc);

		gbc.gridx = 1;
		comboBlackPlayer = new JComboBox<>(playertypes);
		add(comboBlackPlayer, gbc);

		/* * * * * * * * * * * *
		 * Black engine picker *
		 * * * * * * * * * * * */
		gbc.gridx = 0;
		gbc.gridy++;
		JButton blackEngineFromFile = new JButton("Choose File...");
		JLabel blackEnginePath = new JLabel();
		blackEngineFromFile.addActionListener(new EngineChooserButton(blackName, blackEnginePath, Sides.BLACK));

		add(blackEngineFromFile, gbc);
		gbc.gridx = 1;
		add(blackEnginePath, gbc);

		blackEnginePath.setVisible(false);
		blackEngineFromFile.setVisible(false);
		comboBlackPlayer.addActionListener(e -> {
			if (comboBlackPlayer.getSelectedItem().equals("Engine")){
				blackEnginePath.setVisible(true);
				blackEngineFromFile.setVisible(true);
			}
			else{
				blackEnginePath.setVisible(false);
				blackEngineFromFile.setVisible(false);
			}
			NewGame.this.revalidate();
			NewGame.this.repaint();
		});

		
	}

	void addSeparator(GridBagConstraints gbc){
		/* * * * * * *
		 * Separator *
		 * * * * * * */
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy++;
		add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
	}

	void initFEN(GridBagConstraints gbc){
		/* * * * * * *
		 * Start fen *
		 * * * * * * */
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		add(new JLabel("FEN:"), gbc);
		gbc.gridx = 1;
		add(startPos, gbc);
		
	}

	void initTimeSettings(GridBagConstraints gbc){
		/* * * * * * * * *
		 * Time control  *
		 * * * * * * * * */
		ButtonGroup timeControlSelectionGroup = new ButtonGroup();
		
		JRadioButton radioNoControl = new JRadioButton("No time control");
		radioNoControl.setSelected(true);
		radioFischer = new JRadioButton("Fischer");
		radioFixTime = new JRadioButton("Fix time per move");
		timeControl = "N";

		timeControlSelectionGroup.add(radioNoControl);
		timeControlSelectionGroup.add(radioFischer);
		timeControlSelectionGroup.add(radioFixTime);
		
		String[] presets = {"No preset", "Bullet 1+0", "Blitz 3+2", "Blitz 5+0", "Rapid 5+3", "Rapid 10+10", "Tournament", "WCC"};
		fischerPresets = new JComboBox<>(presets);
		fischerPresets.setSelectedIndex(2);
		fischerPresets.setVisible(false);
		fischerPresets.addActionListener(e -> {
			updatingFromPreset = true;
			if (fischerPresets.getSelectedItem().equals(presets[1])){
				fischerControl.setText("S 60");
			}
			else if (fischerPresets.getSelectedItem().equals(presets[2])){
				fischerControl.setText("S 180 0 2");
			} 
			else if (fischerPresets.getSelectedItem().equals(presets[3])){
				fischerControl.setText("S 300");
			} 
			else if (fischerPresets.getSelectedItem().equals(presets[4])){
				fischerControl.setText("S 300 0 3");
			} 
			else if (fischerPresets.getSelectedItem().equals(presets[5])){
				fischerControl.setText("S 600 0 10");
			} 
			else if (fischerPresets.getSelectedItem().equals(presets[6])){
				fischerControl.setText("S 5400 0 30 40 1800");
			} 
			else if (fischerPresets.getSelectedItem().equals(presets[7])){
				fischerControl.setText("S 7200 40 30 40 1800 60 900");
			}
			updatingFromPreset = false; 
		});

		fischerControl = new JTextField("S 180 0 2");
		fischerControl.setVisible(false);
		fischerControl.getDocument().addDocumentListener(
			new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					if (!updatingFromPreset) fischerPresets.setSelectedIndex(0);
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					if (!updatingFromPreset) fischerPresets.setSelectedIndex(0);
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					if (!updatingFromPreset) fischerPresets.setSelectedIndex(0);
				}
			});

		fixTimeControl = new JTextField("X 60");
		fixTimeControl.setVisible(false);
		
		radioFixTime.addActionListener(e -> {
			fixTimeControl.setVisible(true);

			fischerPresets.setVisible(false);
			fischerControl.setVisible(false);
			
			
			NewGame.this.revalidate();
			NewGame.this.repaint();
		});
		radioNoControl.addActionListener(e -> {
			timeControl = "N";
			fixTimeControl.setVisible(false);
			fischerPresets.setVisible(false);
			fischerControl.setVisible(false);

			
			NewGame.this.revalidate();
			NewGame.this.repaint();
		});
		radioFischer.addActionListener(e -> {
			fixTimeControl.setVisible(false);

			fischerPresets.setVisible(true);
			fischerControl.setVisible(true);

			
			NewGame.this.revalidate();
			NewGame.this.repaint();
		});
		
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
			else {
				if (whiteEngine == null) return;
				w = new Engine(Sides.WHITE, whiteName.getText(), whiteEngine);
			}
			if (comboBlackPlayer.getSelectedItem().equals("Human")){
				b = new Human(Sides.BLACK, blackName.getText());
			}
			else {
				if (blackEngine == null) return;
				b = new Engine(Sides.BLACK, blackName.getText(), blackEngine);	
			}
			
			if (radioFischer.isSelected()){
				timeControl = fischerControl.getText();
			}
			else if (radioFixTime.isSelected()){
				timeControl = fixTimeControl.getText();
			}
			else {
				timeControl = "N";
			}

			GameStarter.startNewGame(
				(startPos.getText().equals("startpos") || startPos.getText().equals("") 
					? "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" : startPos.getText()), 
				w, b, timeControl);

			dispose();
		}
	} 

	private class EngineChooserButton implements ActionListener{
		JLabel path;
		JFileChooser chooser;
		JTextField name;
		Sides side;
		public EngineChooserButton(JTextField name, JLabel whitePathShower, Sides side){
			path = whitePathShower;
			chooser = new JFileChooser();
			this.side = side;
			this.name = name;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int returnVal = chooser.showOpenDialog(NewGame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (side == Sides.WHITE){
					whiteEngine = chooser.getSelectedFile();
				}
				else {
					blackEngine = chooser.getSelectedFile();
				}
				String s = chooser.getSelectedFile().getPath();
				int m = 33;
				if (s.length() < m)
					path.setText(s);
				else
					path.setText("... " + s.substring(s.length() - m + 4, s.length()));

				name.setText(chooser.getSelectedFile().getName());
			}
		}
	}
}
