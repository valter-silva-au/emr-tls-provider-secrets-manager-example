package com.amazonaws.awssamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class EmrTlsFromSecretsManager extends AbstractEmrTlsProvider {
    private static final Logger logger = LoggerFactory.getLogger(EmrTlsFromSecretsManager.class);

    public static final String SM_SSL_EMRCERT = "sm:ssl:emrcert";
    public static final String SM_SSL_EMRPRIVATE = "sm:ssl:emrprivate";

    private String sm_privateKey;
    private String sm_certificate;

    public EmrTlsFromSecretsManager() {
        super();
    }

    @Override
    protected void processInstanceTags(Map<String, String> tags) {
        if (tags != null && !tags.isEmpty()) {
            // Get name of SM secret storing CA public certificate
            sm_certificate = tags.get(SM_SSL_EMRCERT);
            if (sm_certificate == null) {
                logger.warn("Certificate secret tag {} not found", SM_SSL_EMRCERT);
            }

            // Get name of SM secret storing CA certificate private key
            sm_privateKey = tags.get(SM_SSL_EMRPRIVATE);
            if (sm_privateKey == null) {
                logger.warn("Private key secret tag {} not found", SM_SSL_EMRPRIVATE);
            }
        } else {
            logger.warn("No tags found");
        }
    }

    @Override
    protected void getCertificates() {
        if (sm_privateKey == null || sm_certificate == null) {
            throw new IllegalStateException("Secret names not initialized. Ensure tags are processed first.");
        }

        // Configure the TLS artifacts manager with the secret names
        tlsArtifactsManager.setSecretNames(sm_privateKey, sm_certificate);
    }
}
