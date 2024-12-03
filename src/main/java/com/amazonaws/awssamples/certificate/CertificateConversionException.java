package com.amazonaws.awssamples.certificate;

public class CertificateConversionException extends RuntimeException {
    public CertificateConversionException(String message) {
        super(message);
    }

    public CertificateConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
