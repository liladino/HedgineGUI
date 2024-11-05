package graphics;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import utility.Pair;
import java.awt.Color;

public class GraphicSettings {
	public static Map<String, Pair<Color, Color>> colors;
	public static boolean rotateBoard;
	public static String selectedScheme;
	
	private GraphicSettings() {}
	
	public static void initializeGraphicSettings() {
		colors = new TreeMap<>();
		rotateBoard = false;
		selectedScheme = "Gray";
		
		colors.put("Gray", new Pair<>(Color.WHITE, Color.GRAY));
		colors.put("Wood", new Pair<>(new Color(255, 178, 102), new Color(153, 76, 0)));
		colors.put("Green", new Pair<>(new Color(178, 255, 102), new Color(76, 153, 0)));
		colors.put("Candy", new Pair<>(Color.PINK, Color.RED));
		colors.put("Bubble gum", new Pair<>(new Color(204, 0, 204), new Color(76, 0, 153)));
		colors.put("Sunny", new Pair<>(new Color(255, 255, 153), new Color(255, 128, 0)));
	}
}
