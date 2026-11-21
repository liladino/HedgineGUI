package hedgineGUI.game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Subprocess fixture: one scored search, one unscored search, or no bestmove. */
public final class FakeUciEngine {
    public static void main(String[] args) throws Exception {
        int searches = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        for (String line; (line = reader.readLine()) != null;) {
            Files.writeString(Path.of(args[1]), line + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (line.equals("uci")) System.out.println("uciok");
            else if (line.equals("isready")) System.out.println("readyok");
            else if (line.equals("quit")) return;
            else if (line.startsWith("go ") && !args[0].equals("stall")) {
                if (searches++ == 0) {
                    System.out.println("info depth 4 score cp 11");
                    System.out.println("info depth 8 cp 35");
                    System.out.println("info depth 3 score cp 999");
                    System.out.println("bestmove e7e5");
                } else {
                    System.out.println("bestmove b8c6");
                }
            }
            System.out.flush();
        }
    }
}
