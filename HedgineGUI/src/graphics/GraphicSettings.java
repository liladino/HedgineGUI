package graphics;

import java.util.Map;
import java.util.TreeMap;

import utility.Pair;
import java.awt.Color;

public class GraphicSettings {
	private static Map<String, Pair<Color, Color>> colorsP = new TreeMap<>();;
	public static final Map<String, Pair<Color, Color>> colors = colorsP;
	public static boolean rotateBoard;
	public static String selectedScheme;
	
	private GraphicSettings() {}
	
	public static void initializeGraphicSettings() {
		rotateBoard = false;
		selectedScheme = "Gray";
		colorsP.put("Gray", new Pair<>(Color.WHITE, Color.GRAY));
		colorsP.put("Wood", new Pair<>(new Color(255, 178, 102), new Color(153, 76, 0)));
		colorsP.put("Green", new Pair<>(new Color(229, 255, 204), new Color(76, 153, 0)));
		colorsP.put("Candy", new Pair<>(Color.PINK, Color.RED));
		colorsP.put("Bubble gum", new Pair<>(new Color(204, 0, 204), new Color(76, 0, 153)));
		colorsP.put("Sunny", new Pair<>(new Color(255, 255, 153), new Color(255, 128, 0)));
	}
}
