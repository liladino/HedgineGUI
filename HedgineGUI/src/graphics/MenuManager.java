package graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import game.interfaces.VisualChangeListener;
import graphics.dialogs.NewGame;

public class MenuManager implements ActionListener {
	private MainWindow mainWindow;
	
	private JMenuBar menuBar;
	private JMenu file;
	private ArrayList<JMenuItem> fileMenuElements;
	
	private JMenu game;
	private ArrayList<JMenuItem> gameMenuElements;
	
	private JMenu view;
	private ArrayList<JMenuItem> viewMenuElements;
	
	private JMenu colorScheme;
	private ArrayList<JMenuItem> colors;
	
	private VisualChangeListener listener;
	
	public MenuManager(MainWindow mainWindow) {
		this.mainWindow = mainWindow;

		fileMenuElements = new ArrayList<>();
		gameMenuElements = new ArrayList<>();
		viewMenuElements = new ArrayList<>();
		colors = new ArrayList<>();

		file = new JMenu("File");
		game = new JMenu("Game");
		view = new JMenu("View");
		colorScheme = new JMenu("Color scheme");
		setUpMenuBar();
	}	

	private void setUpMenuBar() {
		menuBar = new JMenuBar();
				
		menuBar.add(file);
		fileMenuElements.add(new JMenuItem("Load FEN"));
		fileMenuElements.add(new JMenuItem("Load board"));
		fileMenuElements.add(new JMenuItem("Save FEN"));
		fileMenuElements.add(new JMenuItem("Save PGN"));
		fileMenuElements.add(new JMenuItem("Quit"));
		for (JMenuItem m : fileMenuElements) {
			file.add(m);
			m.addActionListener(this);
		}
				
		menuBar.add(game);
		gameMenuElements.add(new JMenuItem("New game"));
		gameMenuElements.add(new JMenuItem("Resign"));
		for (JMenuItem m : gameMenuElements) {
			game.add(m);
			m.addActionListener(this);
		}
		
		menuBar.add(view);
		viewMenuElements.add(new JMenuItem("Rotate board"));
		viewMenuElements.add(colorScheme);
		
		for (JMenuItem m : viewMenuElements) {
			view.add(m);
			m.addActionListener(this);
		}
		
		for (String s : GraphicSettings.colors.keySet()) {
			colors.add(new JMenuItem(s));
		}
		for (JMenuItem m : colors) {
			colorScheme.add(m);
			m.addActionListener(this);
		}

		mainWindow.setJMenuBar(menuBar);
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		String s = a.getActionCommand();
		 
        System.out.println("\"" + s + "\"" + " selected");
        
        if (s.equals("Quit")) {
        	System.exit(0);
        }
        else if (s.equals("Rotate board")) {
        	GraphicSettings.rotateBoard = !GraphicSettings.rotateBoard;
        	listener.onGameLooksChanged();
        }
        else if (s.equals("New game")){
			NewGame newGameWindow = new NewGame(); 
		}
		else if (GraphicSettings.colors.containsKey(s)) {
        	GraphicSettings.selectedScheme = s;
        	listener.onGameLooksChanged();
        }

	}
	
	public void addGameUpdateListener(VisualChangeListener listener) {
		this.listener = listener;
	}
}
