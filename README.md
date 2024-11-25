# HedgineGUI
Version 1.0.0

## About
`https://github.com/liladino/HedgineGUI/`
A UCI compatible chess GUI, made in pair with `https://github.com/liladino/Hedgine` (unfinished).

## Build & run
### Linux command line
cd to the projectfolder, and use the makefile:
```
make build
make run
```
or simply:
```
make all
```
### VSCode
After using git clone and opening the clone folder, the default Run option should launch the app correctly.

### Eclipse
Open project (HedgineGUI folder, where src and test are located).
Set the working directory to the directory where the project was cloned (the parent of HedgineGUI):
`Project Properties > Run/Debug settings`, select a launch configuration, and in `Edit.. > Arguments` change the working directory to Other.
