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

    public TlsArtifactsManager(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
        this.certificateConverter = new CertificateConverter();
    }

    public void setSecretNames(String privateKeySecret, String certificateSecret) {
        this.privateKeySecret = privateKeySecret;
        this.certificateSecret = certificateSecret;
        this.certificateChainSecret = certificateSecret; // Using same secret for cert and chain
    }

    /**
     * Retrieves and processes TLS artifacts from Secrets Manager
     * @return TLSArtifacts containing the private key and certificates
     */
    public TLSArtifacts getTlsArtifacts() {
        String privateKeyContent = secretsManagerClient.getSecret(privateKeySecret);
        String certificateContent = secretsManagerClient.getSecret(certificateSecret);
        
        PrivateKey privateKey = certificateConverter.getPrivateKey(privateKeyContent);
        List<Certificate> certChain = certificateConverter.getX509FromString(certificateContent);
        List<Certificate> certs = certificateConverter.getX509FromString(certificateContent);

        return new TLSArtifacts(privateKey, certChain, certs);
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
