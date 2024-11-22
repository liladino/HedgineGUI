package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import chess.Move;
import utility.Sides;

public class Engine extends Player {
	private File enginePath;
	private static final Logger logger = Logger.getLogger(Engine.class.getName());
	private Process process;
	private BufferedWriter engineInput;
	private BufferedReader engineOutput;
	private ExecutorService executorService;
	private volatile ArrayList<String> outputs;
	boolean uciok = false;
	
	public Engine(Sides side, String name, File enginePath){
		super(side, name);
		human = false;
		this.enginePath = enginePath;
	}

	@Override
	public void makeMove(Move m) {
		// tell the gameManager, that we have a move ready
		listener.onMoveReady(m);
	}
	
	public void validateEngine() throws IOException {
		if (!enginePath.exists() || enginePath.isDirectory() || !enginePath.isFile() || !enginePath.canExecute()) {
			throw new IOException("The file is not an executable.");
		}
		uciok = false;
		startEngine();
		int current = 0;
		while (!uciok) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
			current++;
			if (current > 20) {
				break;
			}
		}
		stopEngine();
		if (!uciok) throw new IOException("Not UCI compatible engine");
	}

	public void startEngine() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(enginePath.getAbsolutePath());
		//redirecting std error to stdout to listen to all kinds of output
		processBuilder.redirectErrorStream(true); 
		process = processBuilder.start();
		
		engineInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
		engineOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		executorService = Executors.newSingleThreadExecutor();

		// Start listening to the engine's output in a separate thread
		executorService.submit(this::listenToEngineOutput);
		sendCommand("uci");
	}
	
	private void listenToEngineOutput() {
		try {
			String output;
			while ((output = engineOutput.readLine()) != null) {
				System.out.println(output);
				String[] outputParsed = output.split(" ");
				if (outputParsed[0].equals("bestmove")) {
					Move m = new Move(outputParsed[1]);
					if (m != null) makeMove(m);
				}
				else if (outputParsed[0].equals("uciok")) {
					uciok = true;
				}
			}
		} catch (IOException e) {
			logger.info("Error reading engine output: " + e.getMessage());
		}
	}

	public void sendCommand(String command) throws IOException {
		engineInput.write(command + "\n");
		engineInput.flush();
	}

	public void stopEngine() {
		try {
			sendCommand("quit");
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			executorService.shutdown();
		}
	}
}
