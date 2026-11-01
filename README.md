# HedgineGUI

## About
`https://github.com/liladino/HedgineGUI/`
A UCI compatible chess GUI, made in pair with `https://github.com/liladino/Hedgine`.

## Features

### Playing modes

* Two player mode
* Engine vs player
* Engine vs Engine

Supports both drag and drop and clicking modes for move input.

### Standards

 * UCI communication supports most major engines (e.g. Stockfish) (as well as my hobby project, Hedgine) 
 * PGN output for games
 * FEN board state save

## Build & run

### Requirements

* JDK 25 or newer
* Maven 3.9+

### Command line

To create a runnable jar file (dependencies included), cd to the project folder and use
```
mvn package
```
You can then run the jar file with
```
java -jar target/HedgineGUI-3.0.0.jar
```

**Recommended**: Build without running the tests:
```
mvn package -DskipTests
```

To run the project from the source:
```
mvn compile exec:java -Dexec.mainClass=hedgineGUI.main.Main
```

To run the tests only:
```
mvn test
```

The app stores its saves in a `saves` folder in the working directory, and creates it on first start.

### IntelliJ IDEA

Open the project folder, and IntelliJ imports it as a Maven project. Run `hedgineGUI.main.Main`.

### VSCode

Install the Extension Pack for Java, then open the project folder. The project is imported from `pom.xml`, and the default Run option on `hedgineGUI.main.Main` launches the app.

### Bot tournaments

The start screen offers **Play** and **Bot tournament**. In tournament setup,
add one tab per simultaneous game and select a white and black engine executable,
participant names, starting FEN, and time control. Supported time control examples:
`N` (no clock, engines get two seconds per move), `X 60` (seconds per move),
and `S 180 0 2` (three minutes plus two seconds per move).

Use the same participant name and executable across games to combine its standings.
Give different versions distinct names, even when their executable filenames match.
Every game uses separate engine processes; engine objects are never shared.
Wins score 1, draws 0.5, and losses 0. Aborts and errors award no points.

Each round appears in a tab containing a scrollable two-column board grid.
Individual games have Abort and Export buttons. The sidebar can abort the current
round, end the tournament, or export all successfully started games from every
round into a single PGN file, including unfinished games. Once all current games
finish or are aborted, **Next round** opens a new set of manual pairing tabs.
Closing the tournament stops its engines.

Automatic rounds: set **Rounds (repeat these pairings)** in the round setup dialog.
The count includes the round being configured. All games in a round run concurrently;
the next round starts after every game has stopped. Each game in every round gets
fresh engine processes. The same colors, FEN, and time controls are repeated.
Aborting a round or the tournament cancels queued rounds. A startup failure also
cancels queued rounds. Individual game aborts do not cancel the schedule.

Clock duration fields accept decimal seconds or integer milliseconds with an
`ms` prefix: `X 1.5`, `X ms500`, and `S 10.5 0 ms100 40 2.5`.
Move indices (such as 0 and 40) must remain integers. Durations must be
nonnegative exact milliseconds, up to 2,147,483,647 ms; finer precision is rejected.
