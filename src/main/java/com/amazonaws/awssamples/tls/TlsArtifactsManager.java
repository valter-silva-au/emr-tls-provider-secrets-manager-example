package com.amazonaws.awssamples.tls;

import com.amazonaws.services.elasticmapreduce.spi.security.TLSArtifacts;
import com.amazonaws.awssamples.certificate.CertificateConverter;
import com.amazonaws.awssamples.secrets.SecretsManagerClient;
import com.amazonaws.awssamples.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

public class TlsArtifactsManager {
    private static final Logger logger = LoggerFactory.getLogger(TlsArtifactsManager.class);

    private final CertificateConverter certificateConverter;
    private final SecretsManagerClient secretsManagerClient;

    private String privateKeySecret;
    private String certificateSecret;
    private String certificateChainSecret;
    
    // Cache for TLS artifacts
    private volatile TLSArtifacts cachedArtifacts;

    public TlsArtifactsManager(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
        this.certificateConverter = new CertificateConverter();
    }

    public void setSecretNames(String privateKeySecret, String certificateSecret) {
        this.privateKeySecret = privateKeySecret;
        this.certificateSecret = certificateSecret;
        this.certificateChainSecret = certificateSecret; // Using same secret for cert and chain
        // Clear cache when secret names change
        this.cachedArtifacts = null;
    }

    /**
     * Retrieves and processes TLS artifacts from Secrets Manager
     * @return TLSArtifacts containing the private key and certificates
     */
    public TLSArtifacts getTlsArtifacts() {
        // Return cached artifacts if available
        if (cachedArtifacts != null) {
            return cachedArtifacts;
        }

        synchronized (this) {
            // Double-check locking to prevent multiple initializations
            if (cachedArtifacts != null) {
                return cachedArtifacts;
            }

            try {
                String privateKeyContent = secretsManagerClient.getSecret(privateKeySecret);
                String certificateContent = secretsManagerClient.getSecret(certificateSecret);
                
                PrivateKey privateKey = certificateConverter.getPrivateKey(privateKeyContent);
                List<Certificate> certChain = certificateConverter.getX509FromString(certificateContent);
                List<Certificate> certs = certificateConverter.getX509FromString(certificateContent);

                cachedArtifacts = new TLSArtifacts(privateKey, certChain, certs);
                return cachedArtifacts;
            } catch (Exception e) {
                logger.error("Failed to retrieve TLS artifacts", e);
                // If we fail to get new artifacts and have cached ones, use those
                if (cachedArtifacts != null) {
                    logger.warn("Using cached TLS artifacts due to retrieval failure");
                    return cachedArtifacts;
                }
                throw e;
            }
        }
    }

    /**
     * Forces a refresh of the TLS artifacts by clearing the cache
     */
    public void refreshArtifacts() {
        this.cachedArtifacts = null;
    }

    /**
     * Writes TLS artifacts to the filesystem (for debugging purposes)
     * @param rootPath base directory to write files to
     */
    public void writeTlsArtifactsToFiles(String rootPath) {
        try {
            String privateKeyContent = secretsManagerClient.getSecret(privateKeySecret);
            String certificateContent = secretsManagerClient.getSecret(certificateSecret);

            FileUtils.writeCertificate(rootPath + "privateKey.pem", privateKeyContent);
            FileUtils.writeCertificate(rootPath + "trustedCertificates.pem", certificateContent);
            FileUtils.writeCertificate(rootPath + "certificateChain.pem", certificateContent);
        } catch (IOException e) {
            logger.error("Failed to write TLS artifacts to files", e);
        }
    }
}
