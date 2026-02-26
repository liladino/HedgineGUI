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

### Bash

To create a runnable jar file, cd to the projectfolder and use
```
make jar
```
You can then run the jar file with
```
java -jar HedgineGUI.jar 
```

To run the project from the source: 
```
make build
make run
```
The two in one:
```
make all
```

### VSCode

After using git clone and opening the clone folder, the default Run option should launch the app correctly.

### Eclipse

Import the folder HedgineGUI, and the default run configuration should launch the app correctly.
