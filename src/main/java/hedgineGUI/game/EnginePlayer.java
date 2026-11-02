package hedgineGUI.game;

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

import hedgineGUI.core.chess.Move;
import hedgineGUI.game.clock.ClockSnapshot;
import hedgineGUI.utility.Sides;
import hedgineGUI.utility.TimeControl;

/** A Player backed by a UCI-compatible engine process. */
public final class EnginePlayer extends Player {
    private static final Logger LOGGER = Logger.getLogger(EnginePlayer.class.getName());
    private static final long UCI_HANDSHAKE_TIMEOUT_MS = 2_000;
    private static final long READY_TIMEOUT_MS = 5_000;
    /** Time kept back from a fixed move time to cover I/O and process overhead. */
    private static final long MOVE_OVERHEAD_MS = 20;
    /** Shared by all engines so the log keeps the order in which lines were seen. */
    private static final ExecutorService LOG_WRITER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "uci-log");
        thread.setDaemon(true);
        return thread;
    });

    private final File enginePath;
    private final List<String> engineInfo = new ArrayList<>();

    private Process process;
    private BufferedWriter engineInput;
    private BufferedReader engineOutput;
    private ExecutorService outputReader;
    private CountDownLatch uciReady;
    private volatile CountDownLatch engineReady;
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
        waitUntilReady();
    }

    /** Blocks until the engine answers isready, so setup work (e.g. hash clearing) is not charged to the clock. */
    private void waitUntilReady() throws IOException {
        CountDownLatch ready = new CountDownLatch(1);
        engineReady = ready;
        sendCommand("isready");
        try {
            if (!ready.await(READY_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                endGame();
                throw new IOException("Engine did not answer 'readyok' within "
                        + READY_TIMEOUT_MS + " ms");
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            endGame();
            throw new IOException("Interrupted while waiting for readyok", error);
        }
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
                // Taken before anything else, so the engine is not charged for our processing.
                handleEngineOutput(output, System.currentTimeMillis());
            }
        } catch (IOException error) {
            if (running) {
                running = false;
                failMoveRequest(error);
            }
        }
    }

    private void handleEngineOutput(String output, long receivedAtMs) {
        log(output);
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
        if ("readyok".equals(tokens[0])) {
            CountDownLatch ready = engineReady;
            if (ready != null) {
                ready.countDown();
            }
            return;
        }
        if ("bestmove".equals(tokens[0]) && tokens.length >= 2
                && !"(none)".equals(tokens[1])) {
            Move move = new Move(tokens[1]);
            if (!move.isNull()) {
                returnMove(move, receivedAtMs);
            }
        }
    }

    private String buildPositionCommand(Position position) {
        StringBuilder command;
        if ("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1".equals(position.getInitialFen().trim())){
            command = new StringBuilder("position startpos");
        }
        else {
            command = new StringBuilder("position fen ").append(position.getInitialFen());
        }
        
        if (!position.getMoveHistory().isEmpty()) {
            command.append(" moves");
            for (Move move : position.getMoveHistory()) {
                command.append(' ').append(move.toString().trim());
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
            long movetime = Math.min((long) (available * 0.9), available - MOVE_OVERHEAD_MS);
            return "go movetime " + Math.max(1, movetime);
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
        engineInput.write(command);
        engineInput.newLine();
        engineInput.flush();
        log("UCI command: " + command);
    }

    /** Console output can block, so it must never delay engine I/O or the clock. */
    private static void log(String message) {
        LOG_WRITER.execute(() -> LOGGER.info(message));
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
        } catch (IOException _) {
            // Best-effort cleanup.
        }
        try {
            if (engineOutput != null) {
                engineOutput.close();
            }
        } catch (IOException _) {
            // Best-effort cleanup.
        }
        engineInput = null;
        engineOutput = null;
    }
}
