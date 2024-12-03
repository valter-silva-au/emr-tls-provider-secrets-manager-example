package com.amazonaws.awssamples;

import com.amazonaws.services.elasticmapreduce.spi.security.TLSArtifacts;
import com.amazonaws.services.elasticmapreduce.spi.security.TLSArtifactsProvider;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.awssamples.ec2.EC2TagManager;
import com.amazonaws.awssamples.secrets.SecretsManagerClient;
import com.amazonaws.awssamples.tls.TlsArtifactsManager;
import com.amazonaws.awssamples.retry.RetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractEmrTlsProvider extends TLSArtifactsProvider {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEmrTlsProvider.class);

    protected final EC2TagManager ec2TagManager;
    protected final SecretsManagerClient secretsManagerClient;
    protected final TlsArtifactsManager tlsArtifactsManager;
    protected final RetryHandler retryHandler;

    protected String region;
    protected String accountId;
    protected boolean initialized;

    public AbstractEmrTlsProvider() {
        this.retryHandler = new RetryHandler();
        this.ec2TagManager = new EC2TagManager(retryHandler);
        
        // Initialize region and account ID
        this.region = ec2TagManager.getRegion();
        this.accountId = ec2TagManager.getAccountId();
        
        this.secretsManagerClient = new SecretsManagerClient(region);
        this.tlsArtifactsManager = new TlsArtifactsManager(secretsManagerClient);
        
        this.initialized = false;
    }

    /**
     * Interface to EMR TLS
     */
    @Override
    public TLSArtifacts getTlsArtifacts() {
        init();
        return tlsArtifactsManager.getTlsArtifacts();
    }

    private void init() {
        if (initialized) {
            return;
        }

        // Read EC2 tags
        Map<String, String> tags = ec2TagManager.getInstanceTags();
        processInstanceTags(tags);

        // Get certificates
        getCertificates();

        initialized = true;

        // Uncomment to write certificates to disk for debugging
        // String rootPath = "/tmp/certs/";
        // tlsArtifactsManager.writeTlsArtifactsToFiles(rootPath);
    }

    /**
     * Process instance tags to extract necessary secret names
     */
    protected abstract void processInstanceTags(Map<String, String> tags);

    /**
     * Retrieve certificates from Secrets Manager
     */
    protected abstract void getCertificates();
}
