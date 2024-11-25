# Variables
PROJECT_NAME = emr-tls-provider-samples
SOURCE_DIR = src/main/java
TARGET_DIR = target
JAR_FILE = $(TARGET_DIR)/$(PROJECT_NAME)-0.1.jar

# Default target
all: clean install build

# Clean target
clean:
	@echo "Cleaning project..."
	mvn clean

# Install target
install:
	@echo "Installing dependencies..."
	mvn install

# Build target
build: 
	@echo "Building project..."
	mvn package

# Run target (optional)
run: build
	@echo "Running the project..."
	java -jar $(JAR_FILE)

.PHONY: all clean install build run