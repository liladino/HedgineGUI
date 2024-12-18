package graphics;

import java.util.Map;
import java.util.TreeMap;

import utility.Pair;
import java.awt.Color;

/**
 * Works like public global settings for the board panel and menu.
 */
public class GraphicSettings {
	private static Map<String, Pair<Color, Color>> colorsP = new TreeMap<>();
	public static final Map<String, Pair<Color, Color>> colors = colorsP;
	public static boolean rotateBoard;
	public static String selectedScheme;
	
	private GraphicSettings() {}
	
	/**
	 * Initializes the settings. 
	 * Should be called at the beginning of the program
	 */
	public static void initializeGraphicSettings() {
		rotateBoard = false;
		selectedScheme = "Green";
		colorsP.put("Gray", new Pair<>(Color.WHITE, Color.GRAY));
		colorsP.put("Wood", new Pair<>(new Color(255, 178, 102), new Color(153, 76, 0)));
		colorsP.put("Green", new Pair<>(new Color(229, 255, 204), new Color(76, 153, 0)));
		colorsP.put("Candy", new Pair<>(Color.PINK, Color.RED));
		colorsP.put("Sky", new Pair<>(Color.WHITE, new Color(63, 156, 203)));
		colorsP.put("Bubble gum", new Pair<>(new Color(204, 0, 204), new Color(76, 0, 153)));
		colorsP.put("Sunny", new Pair<>(new Color(255, 255, 153), new Color(255, 128, 0)));
	}
}
