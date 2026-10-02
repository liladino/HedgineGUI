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
