package com.amazonaws.awssamples.certificate;

public class PrivateKeyGenerationException extends RuntimeException {
    public PrivateKeyGenerationException(String message) {
        super(message);
    }

    public PrivateKeyGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
