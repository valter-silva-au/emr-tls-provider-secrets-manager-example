package com.amazonaws.awssamples.secrets;

public class SecretRetrievalException extends RuntimeException {
    public SecretRetrievalException(String message) {
        super(message);
    }

    public SecretRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
