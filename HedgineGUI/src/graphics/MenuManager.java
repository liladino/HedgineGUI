package graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuManager implements ActionListener {
	private MainWindow mainWindow;
	
	private JMenuBar menuBar;
	private JMenu file;
	private ArrayList<JMenuItem> fileMenuElements;
	
	private JMenu view;
	private ArrayList<JMenuItem> viewMenuElements;
	
	private JMenu colorScheme;
	private ArrayList<JMenuItem> colors;
	
	public MenuManager(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		setUpMenuBar();
	}	

	private void setUpMenuBar() {
		menuBar = new JMenuBar();
		
		file = new JMenu("File");
		menuBar.add(file);
		
		fileMenuElements = new ArrayList<>();
		fileMenuElements.add(new JMenuItem("Quit"));
		for (JMenuItem m : fileMenuElements) {
			file.add(m);
			m.addActionListener(this);
		}
		
		view = new JMenu("View");
		menuBar.add(view);
		
		viewMenuElements = new ArrayList<>();
		viewMenuElements.add(new JMenuItem("Rotate board"));
		colorScheme = new JMenu("Color scheme");
		viewMenuElements.add(colorScheme);
		
		for (JMenuItem m : viewMenuElements) {
			view.add(m);
			m.addActionListener(this);
		}
		
		colors = new ArrayList<>();
		colors.add(new JMenuItem("Gray"));
		colors.add(new JMenuItem("Bubble gum"));
		colors.add(new JMenuItem("Wood"));
		colors.add(new JMenuItem("Green"));
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
	}
}
