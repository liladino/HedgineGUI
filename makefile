SRC_DIR = HedgineGUI/src
BIN_DIR = HedgineGUI/bin
LIB_DIR = HedgineGUI/lib
MAIN_CLASS = main.Main

SOURCES = $(shell find $(SRC_DIR) -name "*.java")

CLASSPATH = $(LIB_DIR)/*:$(BIN_DIR)

build:
	make clean
	mkdir -p $(BIN_DIR)
	javac -cp $(CLASSPATH) -d $(BIN_DIR) $(SOURCES)

run:
	java -cp $(CLASSPATH) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)

all: build
	make run
