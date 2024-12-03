# Makefile for EMR TLS Provider project

# Variables
MAVEN = mvn
TARGET_DIR = target
JAR_NAME = emr-tls-provider-samples-0.1-jar-with-dependencies.jar
CERT_DIR = test-certs
KEYSTORE_DIR = test-keystores
KEYSTORE_PASSWORD = changeit

# Default target
.DEFAULT_GOAL := help

# Targets
.PHONY: clean compile test package all help clean-certs setup-test-dirs generate-test-certs create-stores verify-stores integration-test

clean: clean-certs ## Clean the project (remove target directory)
	@echo "Cleaning project..."
	@$(MAVEN) clean

clean-certs: ## Clean test certificates and keystores
	@echo "Cleaning certificates and keystores..."
	@rm -rf $(CERT_DIR) $(KEYSTORE_DIR)

compile: ## Compile the source code
	@echo "Compiling source code..."
	@$(MAVEN) compile

test: ## Run unit tests
	@echo "Running tests..."
	@$(MAVEN) test

package: ## Create JAR with dependencies
	@echo "Creating JAR with dependencies..."
	@$(MAVEN) package

setup-test-dirs: clean-certs ## Create directories for test certificates and keystores
	@echo "Creating test directories..."
	@mkdir -p $(CERT_DIR)
	@mkdir -p $(KEYSTORE_DIR)

generate-test-certs: setup-test-dirs ## Generate test certificates using OpenSSL
	@echo "Generating test certificates..."
	@openssl req -x509 -newkey rsa:2048 -keyout $(CERT_DIR)/private.key -nodes \
		-out $(CERT_DIR)/certificate.pem -sha256 -days 365 \
		-subj "/C=US/ST=Washington/L=Seattle/O=Test/OU=EMR/CN=localhost"
	@openssl pkcs8 -topk8 -inform PEM -in $(CERT_DIR)/private.key \
		-out $(CERT_DIR)/private.pkcs8.key -nocrypt
	@echo "Certificates generated in $(CERT_DIR)"

create-stores: generate-test-certs ## Create keystore and truststore
	@echo "Creating keystore and truststore..."
	@keytool -import -noprompt -trustcacerts \
		-alias testcert -file $(CERT_DIR)/certificate.pem \
		-keystore $(KEYSTORE_DIR)/truststore.jks \
		-storepass $(KEYSTORE_PASSWORD)
	@openssl pkcs12 -export -in $(CERT_DIR)/certificate.pem \
		-inkey $(CERT_DIR)/private.pkcs8.key \
		-out $(CERT_DIR)/keystore.p12 -name testcert \
		-passout pass:$(KEYSTORE_PASSWORD)
	@keytool -importkeystore -noprompt \
		-srckeystore $(CERT_DIR)/keystore.p12 -srcstoretype PKCS12 \
		-srcstorepass $(KEYSTORE_PASSWORD) \
		-destkeystore $(KEYSTORE_DIR)/keystore.jks \
		-deststorepass $(KEYSTORE_PASSWORD)
	@echo "Keystores created in $(KEYSTORE_DIR)"

verify-stores: ## Verify keystore and truststore contents
	@echo "Verifying keystore contents..."
	@keytool -list -keystore $(KEYSTORE_DIR)/keystore.jks \
		-storepass $(KEYSTORE_PASSWORD)
	@echo "\nVerifying truststore contents..."
	@keytool -list -keystore $(KEYSTORE_DIR)/truststore.jks \
		-storepass $(KEYSTORE_PASSWORD)

integration-test: create-stores ## Run integration tests with generated certificates
	@echo "Running integration tests..."
	@$(MAVEN) verify -DskipTests=false \
		-Djavax.net.ssl.keyStore=$(KEYSTORE_DIR)/keystore.jks \
		-Djavax.net.ssl.keyStorePassword=$(KEYSTORE_PASSWORD) \
		-Djavax.net.ssl.trustStore=$(KEYSTORE_DIR)/truststore.jks \
		-Djavax.net.ssl.trustStorePassword=$(KEYSTORE_PASSWORD)

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
