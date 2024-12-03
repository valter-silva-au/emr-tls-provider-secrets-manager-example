# EMR TLS Custom Certificate Provider

This project provides custom TLS certificate provider implementations for Amazon EMR clusters, enabling secure node-to-node communication. The implementation demonstrates how to integrate EMR clusters with AWS Secrets Manager for certificate management.

## Overview

The EMR TLS Custom Certificate Provider allows you to manage TLS certificates for your EMR cluster nodes using AWS Secrets Manager. This enables secure communication between nodes while leveraging AWS's managed secret storage service for certificate management.

## Tech Stack

### Core Technology
- Java 8
- Maven (Build Tool)

### AWS SDK Components (v1.11.339)
- aws-java-sdk-emr: Amazon EMR integration
- aws-java-sdk-secretsmanager: AWS Secrets Manager integration
- aws-java-sdk-core: Core AWS SDK functionality
- aws-java-sdk-ec2: Amazon EC2 integration
- aws-java-sdk-acm: AWS Certificate Manager integration

### Additional Libraries
- Apache Commons Lang3 (v3.12.0): Java utility functions
- Bouncy Castle (v1.70):
  - bcpkix-jdk14: Cryptography operations
  - bcprov-jdk14: Cryptographic provider functionality

## Prerequisites

- Java 8 or later
- Maven 3.x
- AWS Account with access to:
  - Amazon EMR
  - AWS Secrets Manager
  - Amazon EC2
  - AWS Certificate Manager

## Building the Project

To build the project, run:

```bash
mvn clean package
```

This will create a JAR file with all dependencies included.

## Implementation Details

The project includes the following main components:

- `EmrTlsFromSecretsManager`: Implementation of TLS Custom Certificate provider using AWS Secrets Manager
- `AbstractEmrTlsProvider`: Base abstract class for TLS provider implementations

## Usage

The provider can be configured as part of your EMR cluster setup to manage TLS certificates for node-to-node communication. The implementation retrieves certificates from AWS Secrets Manager and manages them for your EMR cluster nodes.

## License

This sample code is made available under a modified MIT license. See the LICENSE file for details.

## Security

See [CONTRIBUTING](CONTRIBUTING.md) for more information.

## Code of Conduct

See [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md) for more information.
