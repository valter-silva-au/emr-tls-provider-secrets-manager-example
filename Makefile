# Makefile for EMR TLS Provider project

# Variables
MAVEN = mvn
TARGET_DIR = target
JAR_NAME = emr-tls-provider-samples-0.1-jar-with-dependencies.jar

# Default target
.DEFAULT_GOAL := help

# Targets
.PHONY: clean compile test package all help

clean: ## Clean the project (remove target directory)
	@echo "Cleaning project..."
	@$(MAVEN) clean

compile: ## Compile the source code
	@echo "Compiling source code..."
	@$(MAVEN) compile

test: ## Run unit tests
	@echo "Running tests..."
	@$(MAVEN) test

package: ## Create JAR with dependencies
	@echo "Creating JAR with dependencies..."
	@$(MAVEN) package

all: clean compile test package ## Build the complete project (clean, compile, test, package)

verify-jar: ## Verify the JAR file exists
	@if [ -f "$(TARGET_DIR)/$(JAR_NAME)" ]; then \
		echo "JAR file successfully created at: $(TARGET_DIR)/$(JAR_NAME)"; \
	else \
		echo "Error: JAR file not found!"; \
		exit 1; \
	fi

help: ## Display this help message
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
