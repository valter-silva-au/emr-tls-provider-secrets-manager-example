package com.amazonaws.awssamples.secrets;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.retry.RetryUtils;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.AWSSecretsManagerException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.awssamples.retry.RetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Base64;

public class SecretsManagerClient {
    private static final Logger logger = LoggerFactory.getLogger(SecretsManagerClient.class);
    private final String region;
    private final AWSSecretsManager client;
    private final RetryHandler retryHandler;

    public SecretsManagerClient(String region) {
        this.region = region;
        this.client = createClient();
        this.retryHandler = new RetryHandler();
    }

    private AWSSecretsManager createClient() {
        String endpoint = String.format("secretsmanager.%s.amazonaws.com", region);
        AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        return AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(config)
                .build();
    }

    /**
     * Retrieves a secret from AWS Secrets Manager
     * @param secretName name of the secret to retrieve
     * @return the secret value
     * @throws SecretRetrievalException if the secret cannot be retrieved
     */
    public String getSecret(String secretName) {
        logger.debug("Retrieving secret: {}", secretName);
        
        GetSecretValueRequest request = new GetSecretValueRequest()
                .withSecretId(secretName)
                .withVersionStage("AWSCURRENT");

        GetSecretValueResult result = retryHandler.retry(req -> {
            try {
                return client.getSecretValue(req);
            } catch (AWSSecretsManagerException ex) {
                if (RetryUtils.isThrottlingException(ex)) {
                    logger.warn("Got throttling exception while retrieving secret", ex);
                    return null;
                }
                logger.error("Failed to retrieve secret", ex);
                throw new SecretRetrievalException("Failed to retrieve secret: " + secretName, ex);
            }
        }, request);

        if (result == null) {
            throw new SecretRetrievalException("Failed to retrieve secret after retries: " + secretName);
        }

        return parseSecretValue(result);
    }

    private String parseSecretValue(GetSecretValueResult result) {
        if (result.getSecretString() != null) {
            return result.getSecretString();
        }
        
        ByteBuffer binarySecretData = result.getSecretBinary();
        if (binarySecretData == null) {
            throw new SecretRetrievalException("Secret value is empty");
        }
        
        return new String(Base64.getDecoder().decode(binarySecretData).array());
    }
}
