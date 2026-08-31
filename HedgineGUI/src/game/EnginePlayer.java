package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import core.chess.Move;
import game.clock.ClockSnapshot;
import utility.Sides;
import utility.TimeControl;

/** A Player backed by a UCI-compatible engine process. */
public final class EnginePlayer extends Player {
    private static final Logger LOGGER = Logger.getLogger(EnginePlayer.class.getName());
    private static final long UCI_HANDSHAKE_TIMEOUT_MS = 2_000;

    private final File enginePath;
    private final List<String> engineInfo = new ArrayList<>();

    private Process process;
    private BufferedWriter engineInput;
    private BufferedReader engineOutput;
    private ExecutorService outputReader;
    private CountDownLatch uciReady;
    private volatile boolean running;

    public EnginePlayer(Sides side, String name, File enginePath) {
        super(side, name);
        this.enginePath = enginePath;
    }

    @Override
    public synchronized void startGame() throws IOException {
        if (!running) {
            startEngine();
        }
        sendCommand("ucinewgame");
    }

    @Override
    protected void onMoveRequested(Position position) throws IOException {
        if (!running) {
            startGame();
        }
        sendCommand(buildPositionCommand(position));
        sendCommand(buildGoCommand(position));
    }

    @Override
    protected void onMoveRequestCancelled() {
        if (!running) {
            return;
        }
        try {
            sendCommand("stop");
        } catch (IOException error) {
            LOGGER.fine("Could not stop engine search: " + error.getMessage());
        }
    }

    @Override
    public synchronized void endGame() {
        super.endGame();
        if (!running) {
            return;
        }

        try {
            sendCommand("quit");
        } catch (IOException error) {
            LOGGER.fine("Could not send quit to engine: " + error.getMessage());
        } finally {
            running = false;
            if (process != null) {
                process.destroy();
            }
            if (outputReader != null) {
                outputReader.shutdownNow();
            }
            closeStreams();
        }
    }

    private void startEngine() throws IOException {
        validateExecutable();

        ProcessBuilder processBuilder = new ProcessBuilder(enginePath.getAbsolutePath());
        processBuilder.redirectErrorStream(true);
        process = processBuilder.start();
        engineInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        engineOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        outputReader = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "uci-output-" + getName());
            thread.setDaemon(true);
            return thread;
        });
        uciReady = new CountDownLatch(1);
        running = true;

        outputReader.submit(this::listenToEngineOutput);
        sendCommand("uci");

        try {
            if (!uciReady.await(UCI_HANDSHAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                endGame();
                throw new IOException("Engine did not answer 'uciok' within "
                        + UCI_HANDSHAKE_TIMEOUT_MS + " ms");
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            endGame();
            throw new IOException("Interrupted while waiting for UCI handshake", error);
        }
    }

    private void validateExecutable() throws IOException {
        if (enginePath == null || !enginePath.isFile() || !enginePath.canExecute()) {
            throw new IOException("Engine path is not an executable file");
        }
    }

    private void listenToEngineOutput() {
        try {
            String output;
            while (running && (output = engineOutput.readLine()) != null) {
                handleEngineOutput(output);
            }
        } catch (IOException error) {
            if (running) {
                running = false;
                failMoveRequest(error);
            }
        }
    }

    private void handleEngineOutput(String output) {
        LOGGER.fine(output);
        synchronized (engineInfo) {
            engineInfo.add(output);
        }

        String[] tokens = output.trim().split("\\s+");
        if (tokens.length == 0) {
            return;
        }
        if ("uciok".equals(tokens[0])) {
            uciReady.countDown();
            return;
        }
        if ("bestmove".equals(tokens[0]) && tokens.length >= 2
                && !"(none)".equals(tokens[1])) {
            Move move = new Move(tokens[1]);
            if (!move.isNull()) {
                returnMove(move);
            }
        }
    }

    private String buildPositionCommand(Position position) {
        StringBuilder command = new StringBuilder("position fen ")
                .append(position.getInitialFen());
        if (!position.getMoveHistory().isEmpty()) {
            command.append(" moves");
            for (Move move : position.getMoveHistory()) {
                command.append(' ').append(move);
            }
        }
        return command.toString();
    }

    private String buildGoCommand(Position position) {
        ClockSnapshot clock = position.getClock();
        if (clock == null || clock.getTimeControl() == TimeControl.NO_CONTROL) {
            return "go movetime 2000";
        }
        if (clock.getTimeControl() == TimeControl.FIX_TIME_PER_MOVE) {
            long available = getSide() == Sides.WHITE
                    ? clock.getWhiteTimeMs()
                    : clock.getBlackTimeMs();
            return "go movetime " + Math.max(1, (long) (available * 0.9));
        }

        return new StringBuilder("go wtime ")
                .append(clock.getWhiteTimeMs())
                .append(" btime ").append(clock.getBlackTimeMs())
                .append(" winc ").append(clock.getWincMs())
                .append(" binc ").append(clock.getBincMs())
                .toString();
    }

    public synchronized void sendCommand(String command) throws IOException {
        if (!running || engineInput == null) {
            throw new IOException("Engine is not running");
        }
        LOGGER.fine("UCI command: " + command);
        engineInput.write(command);
        engineInput.newLine();
        engineInput.flush();
    }

    public boolean isRunning() {
        return running;
    }

    public String getInfo() {
        synchronized (engineInfo) {
            return String.join(System.lineSeparator(), engineInfo);
        }
    }

    private void closeStreams() {
        try {
            if (engineInput != null) {
                engineInput.close();
            }
        } catch (IOException ignored) {
            // Best-effort cleanup.
        }
        try {
            if (engineOutput != null) {
                engineOutput.close();
            }
        } catch (IOException ignored) {
            // Best-effort cleanup.
        }
        engineInput = null;
        engineOutput = null;
    }
}
