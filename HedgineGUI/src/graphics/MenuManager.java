package graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

import chess.Board;
import game.GameStarter;
import game.interfaces.VisualChangeListener;
import graphics.dialogs.InformationDialogs;
import graphics.dialogs.NewGame;

public class MenuManager implements ActionListener {
	private MainWindow mainWindow;
	
	private JMenu file;
	private ArrayList<String> fileMenuStrings;

	private JMenu game;
	private ArrayList<String> gameMenuStrings;

	private JMenu view;
	private ArrayList<JMenuItem> viewMenuElements;
	private ArrayList<String> viewMenuStrings;

	private JMenu colorScheme;
	private ArrayList<JMenuItem> colors;
	
	private VisualChangeListener listener;
	
	public MenuManager(MainWindow mainWindow) {
		this.mainWindow = mainWindow;

		fileMenuStrings = new ArrayList<>();
		gameMenuStrings = new ArrayList<>();
		viewMenuStrings = new ArrayList<>();
		viewMenuElements = new ArrayList<>();
		
		colors = new ArrayList<>();

		file = new JMenu("File");
		game = new JMenu("Game");
		view = new JMenu("View");
		setUpMenuBar();
	}	

	private void setUpMenuBar() {
		JMenuBar menuBar = new JMenuBar();
				
		menuBar.add(file);

		fileMenuStrings.add("Load FEN");		//0
		fileMenuStrings.add("Load board");	//1
		fileMenuStrings.add("Save FEN");		//2
		fileMenuStrings.add("Save PGN");		//3
		fileMenuStrings.add("Save board");	//4
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
		for (String s : gameMenuStrings){
			JMenuItem m = new JMenuItem(s);
			game.add(m);
			m.addActionListener(this);
		}
		
		menuBar.add(view);
		viewMenuStrings.add("Rotate board");
		viewMenuStrings.add("Color scheme");
		colorScheme = new JMenu(viewMenuStrings.get(1));
		viewMenuElements.add(new JMenuItem(viewMenuStrings.get(0)));
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
        
        if (s.equals(fileMenuStrings.get(5))) {
        	System.exit(0);
        }
        else if (s.equals(viewMenuStrings.get(0))) {
			//rotate
        	GraphicSettings.rotateBoard = !GraphicSettings.rotateBoard;
        	listener.onGameLooksChanged();
        }
        else if (s.equals(gameMenuStrings.get(0))){
			//new game
			new NewGame();
		}
		else if (GraphicSettings.colors.containsKey(s)) {
			//color scheme
        	GraphicSettings.selectedScheme = s;
        	listener.onGameLooksChanged();
        }
		else if (s.equals(fileMenuStrings.get(0))){
			//load fen
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter("FEN files", "fen", "FEN", "txt"));
			chooser.setDialogTitle(fileMenuStrings.get(0));

			int returnVal = chooser.showOpenDialog(mainWindow);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try{ 
					Scanner scanner = new Scanner(chooser.getSelectedFile());
					if (scanner.hasNextLine()){
						new NewGame(scanner.nextLine());
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
			String fen = GameStarter.getGameManager().getBoard().convertToFEN();
			
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(fileMenuStrings.get(2));

			int returnVal = chooser.showSaveDialog(mainWindow);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try(FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".fen")) {
					fw.write(fen);
				} catch (Exception ex) {
					ex.printStackTrace();
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
				InformationDialogs.errorDialog(mainWindow, "Saved not found: " + f.getMessage());
			} catch (IOException i) {
				InformationDialogs.errorDialog(mainWindow, "Problem while opening: " + i.getMessage());
			}
			catch (ClassNotFoundException c){
				return;
			}
			if (temp != null) new NewGame(temp.convertToFEN());
		}
		else if (s.equals(fileMenuStrings.get(4))){
			//save board
			try (
				FileOutputStream fout = new FileOutputStream(System.getProperty("user.dir") + "/saves/board.ser");
				ObjectOutputStream oos = new ObjectOutputStream(fout);
			){
				oos.writeObject(GameStarter.getGameManager().getBoard());
			}
			catch (IOException i){
				InformationDialogs.errorDialog(mainWindow, "Problem while saving the file: " + i.getMessage());
			}
		}
	}
	
	public void addGameUpdateListener(VisualChangeListener listener) {
		this.listener = listener;
	}
}
