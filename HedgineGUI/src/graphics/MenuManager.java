package graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.management.RuntimeErrorException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

import control.GameController;
import core.chess.Board;
import game.EnginePlayer;
import game.GameEventListener;
import game.Player;
import graphics.dialogs.InformationDialogs;
import graphics.dialogs.NewGame;
import utility.Result;

/**
 * Initializes the menubar, and handles the interactions.
 */
public class MenuManager implements ActionListener {
	private static final Logger logger = Logger.getLogger(MenuManager.class.getName());
	private MainWindow mainWindow;
	private GameController gameController;
	
	private JMenu file;
	private ArrayList<String> fileMenuStrings;
	private JMenu game;
	private ArrayList<String> gameMenuStrings;
	private JMenu view;
	private ArrayList<JMenuItem> viewMenuElements;
	private ArrayList<JMenuItem> colors;
	private ArrayList<JMenuItem> inputModes;
	private ArrayList<String> inputModesStrings;
	private ArrayList<String> viewMenuStrings;
	private JMenu engines;
	private ArrayList<String> enginesMenuStrings;

	private JMenu about;
	private ArrayList<String> aboutMenuStrings;
	
	private ArrayList<GameEventListener> gameEventListeners;
	private ArrayList<Runnable> appearanceListeners;


	public MenuManager(MainWindow mainWindow, GameController gameController) {
		this.mainWindow = mainWindow;
		this.gameController = gameController;

		fileMenuStrings = new ArrayList<>();
		gameMenuStrings = new ArrayList<>();
		viewMenuStrings = new ArrayList<>();
		viewMenuElements = new ArrayList<>();
		enginesMenuStrings = new ArrayList<>();
		gameEventListeners = new ArrayList<>();
		appearanceListeners = new ArrayList<>();
		aboutMenuStrings = new ArrayList<>();

		colors = new ArrayList<>();
		inputModes = new ArrayList<>();
		inputModesStrings = new ArrayList<>();

		file = new JMenu("File");
		game = new JMenu("Game");
		view = new JMenu("View");
		engines = new JMenu("Engine");
		about = new JMenu("About");
		setUpMenuBar();
	}	

	private void setUpMenuBar() {
		JMenuBar menuBar = new JMenuBar();
				
		menuBar.add(file);

		fileMenuStrings.add("Load FEN");		//0
		fileMenuStrings.add("Load board");	  //1
		fileMenuStrings.add("Save FEN");		//2
		fileMenuStrings.add("Save PGN");		//3
		fileMenuStrings.add("Quit");			//5
		
		for (String s : fileMenuStrings){
			JMenuItem m = new JMenuItem(s);
			file.add(m);
			m.addActionListener(this);
		}
				
		menuBar.add(game);
		gameMenuStrings.add("New game");
		gameMenuStrings.add("Resing");
		gameMenuStrings.add("Take back");
		gameMenuStrings.add("Abort");
		for (String s : gameMenuStrings){
			JMenuItem m = new JMenuItem(s);
			game.add(m);
			m.addActionListener(this);
		}
		
		menuBar.add(view);
		viewMenuStrings.add("Rotate board");
		viewMenuStrings.add("Color scheme");
		viewMenuStrings.add("Input mode");
		JMenu colorScheme = new JMenu(viewMenuStrings.get(1));
		JMenu inputModeSel = new JMenu(viewMenuStrings.get(2));
		
		viewMenuElements.add(new JMenuItem(viewMenuStrings.get(0)));
		
		viewMenuElements.add(colorScheme);
		viewMenuElements.add(inputModeSel);
		
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
		inputModesStrings.add("Drag and drop");
		inputModesStrings.add("Click");
		for (String s : inputModesStrings) {
			inputModes.add(new JMenuItem(s));	
		}
		for (JMenuItem m : inputModes) {
			inputModeSel.add(m);
			m.addActionListener(this);
		}
		
		menuBar.add(engines);

		enginesMenuStrings.add("Quit engine(s)");
		enginesMenuStrings.add("Restart engine");
		enginesMenuStrings.add("Stop engine");
		enginesMenuStrings.add("Engine info");
		
		for (String s : enginesMenuStrings){
			JMenuItem m = new JMenuItem(s);
			engines.add(m);
			m.addActionListener(this);
		}

		aboutMenuStrings.add("Hedine GUI");
		for (String s : aboutMenuStrings){
			JMenuItem m = new JMenuItem(s);
			about.add(m);
			m.addActionListener(this);
		}
		
		menuBar.add(about);
		
		mainWindow.setJMenuBar(menuBar);
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		String s = a.getActionCommand();
		 
		logger.info("\"" + s + "\"" + " selected");
		
		if (s.equals(fileMenuStrings.get(4))) {
			gameController.stopGame();
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			System.exit(0);
		}
		else if (s.equals(viewMenuStrings.get(0))) {
			//rotate
			GraphicSettings.rotateBoard = !GraphicSettings.rotateBoard;
			for (GameEventListener listener : gameEventListeners) {
				listener.onGameLooksChanged();	
			}
			notifyAppearanceListeners();
		}
		else if (s.equals(gameMenuStrings.get(0))){
			//new game
			new NewGame(gameController);
		}
		else if (GraphicSettings.colors.containsKey(s)) {
			//color scheme
			GraphicSettings.selectedScheme = s;
			for (GameEventListener listener : gameEventListeners) {
				listener.onGameLooksChanged();	
			}
			notifyAppearanceListeners();
		}
		else if (inputModesStrings.contains(s)) {
			//input mode
			//System.out.println("kurvaelet");
			if (inputModesStrings.get(0).equals(s)) {
				GraphicSettings.dragDrop = true;				
			}
			else {
				GraphicSettings.dragDrop = false;
			}
			for (GameEventListener listener : gameEventListeners) {
				listener.onGameLooksChanged();	
			}
			notifyAppearanceListeners();
		}
		else if (s.equals(fileMenuStrings.get(0))){
			//load fen
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("FEN files", "fen", "FEN", "txt"));
			chooser.setDialogTitle(fileMenuStrings.get(0));
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/saves"));

			int returnVal = chooser.showOpenDialog(mainWindow);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try{ 
					Scanner scanner = new Scanner(chooser.getSelectedFile());
					if (scanner.hasNextLine()){
						new NewGame(scanner.nextLine(), gameController);
						scanner.close();
						return;
					}
					scanner.close();
					InformationDialogs.errorDialog(mainWindow, "Can't load FEN");
				}
				catch(FileNotFoundException f){
					InformationDialogs.errorDialog(mainWindow, "Can't open file");
				}
			}

		}
		else if (s.equals(fileMenuStrings.get(2))){
			//save fen
			String fen = gameController.getFEN();
			
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(fileMenuStrings.get(2));
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/saves"));

			int returnVal = chooser.showSaveDialog(mainWindow);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try(FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".fen")) {
					fw.write(fen);
				} catch (Exception e) {
					InformationDialogs.errorDialog(mainWindow, "Problem while writing: " + e.getMessage());
				}
			}
		}
		else if (s.equals(fileMenuStrings.get(1))){
			//load board
			Board temp = null;
			try (
				FileInputStream fin = new FileInputStream(System.getProperty("user.dir") + "/saves/board.ser");
				ObjectInputStream oin = new ObjectInputStream(fin);
			){
				temp = (Board) oin.readObject();
			} catch (FileNotFoundException f) {
				InformationDialogs.errorDialog(mainWindow, "Save not found: " + f.getMessage());
			} catch (IOException i) {
				InformationDialogs.errorDialog(mainWindow, "Problem while opening: " + i.getMessage());
			}
			catch (ClassNotFoundException c){
				return;
			}
			if (temp != null) new NewGame(temp.convertToFEN(), gameController);
		}
		else if (s.equals(fileMenuStrings.get(3))){
			//save pgn
			String pgn = gameController.getPGN();
			
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(fileMenuStrings.get(3));
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/saves"));

			int returnVal = chooser.showSaveDialog(mainWindow);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try(FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".pgn")) {
					fw.write(pgn);
				} catch (Exception e) {
					InformationDialogs.errorDialog(mainWindow, "Problem while writing: " + e.getMessage());
				}
			}
		}
		else if (s.equals(gameMenuStrings.get(1))){
			//resign
			if (Result.ONGOING != gameController.getState().getResult()) return;
			
			gameController.resign();

			for (GameEventListener g : gameEventListeners){
				g.onResign(gameController.getState().getWinner());
			}
		}
		else if (s.equals(gameMenuStrings.get(2))){
			//take back
			gameController.smartTakeBack();
		}
		else if (s.equals(gameMenuStrings.get(3))){
			//abort
			if (Result.ONGOING != gameController.getState().getResult()) return;
			
			gameController.stopGame();
			
			for (GameEventListener g : gameEventListeners){
				g.onDraw();
			}
		}
		else if (s.equals(enginesMenuStrings.get(0))){
			//quit engines
			
			gameController.quitEngines();
		}
		else if (s.equals(enginesMenuStrings.get(1))){
			//restart engine
			EnginePlayer e = getEngine();
			if (e == null) return;
			try {
				if (e.isRunning()) e.endGame();
				Thread.sleep(100);
				e.startGame();
				gameController.requestMove();
			} catch (IOException e1) {
				logger.info("Error while communicating with engine");
				logger.info(e1.getMessage());
			}
			catch (InterruptedException ir) {
				logger.info(ir.getMessage());
			}
		}
		else if (s.equals(enginesMenuStrings.get(2))){
			//engine stop -> get move
			EnginePlayer e = getEngine();
			if (e == null) return;
			try {
				if (e.isRunning()) e.sendCommand("stop");
			} catch (IOException e1) {
				return;
			}
		}
		else if (s.equals(enginesMenuStrings.get(3))){
			//engine info
			EnginePlayer e = getEngine();
			if (e == null) return;
			e.getInfo();
		}
		else if (s.equals(aboutMenuStrings.get(0))) {
			InformationDialogs.aboutDialog(mainWindow);
		}
	}
	
	private EnginePlayer getEngine() {
		Player p = gameController.getCurrentPlayer();
		if (p instanceof EnginePlayer){
			EnginePlayer e = (EnginePlayer)p;
			return e;
		}
		return null;
	}
	
	public void addGameEventListener(GameEventListener listener) {
		gameEventListeners.add(listener);
	}

	public void addAppearanceListener(Runnable listener) {
		appearanceListeners.add(listener);
	}

	private void notifyAppearanceListeners() {
		for (Runnable listener : appearanceListeners) {
			listener.run();
		}
	}
}
