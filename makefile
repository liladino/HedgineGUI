SRC_DIR = HedgineGUI/src
BIN_DIR = HedgineGUI/bin
LIB_DIR = HedgineGUI/lib
RESOURCES_DIR = HedgineGUI/src/resources
MAIN_CLASS = main.Main
JAR_FILE = HedgineGUI.jar

SOURCES = $(shell find $(SRC_DIR) -name "*.java")

CLASSPATH = $(LIB_DIR)/*:$(BIN_DIR)

build:
	make clean
	mkdir -p $(BIN_DIR)
	javac -cp $(CLASSPATH) -d $(BIN_DIR) $(SOURCES)
	cp -r $(RESOURCES_DIR) $(BIN_DIR)

run:
	java -cp $(CLASSPATH) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)

jar: build
	echo "Main-Class: $(MAIN_CLASS)" > MANIFEST.MF
	jar cfm $(JAR_FILE) MANIFEST.MF -C $(BIN_DIR) .
	rm MANIFEST.MF
	chmod +x $(JAR_FILE)

all: build
	make run
