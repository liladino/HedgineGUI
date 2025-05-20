# HedgineGUI
Version 1.2.1

## About
`https://github.com/liladino/HedgineGUI/`
A UCI compatible chess GUI, made in pair with `https://github.com/liladino/Hedgine` (unfinished).

## Features

### Playing modes

* Two player moe
* Engine vs player
* Engie vs Engine

Supports both drag and drop and clickong modes for move input.

### Standard communication

 * UCI: Supports Stockfish, Hedgine, and most major engines
 * PGN output for games
 * FEN board state save

## Build & run

### Linux command line

To create a runnable jar file, cd to the projectfolder and use:
```
make jar
```

To run the project from the command line: 
```
make build
make run
```
the two in one:
```
make all
```

### VSCode

After using git clone and opening the clone folder, the default Run option should launch the app correctly.

### Eclipse

Import the folder HedgineGUI, and the default run configuration should launch the app correctly.
