package com.amazonaws.awssamples;

import com.amazonaws.awssamples.tls.TlsArtifactsManager;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class TlsProviderIT {
    private static final String KEYSTORE_PATH = "test-keystores/keystore.jks";
    private static final String TRUSTSTORE_PATH = "test-keystores/truststore.jks";
    private static final String STORE_PASSWORD = "changeit";
    private static final String CERT_ALIAS = "testcert";

    @Test
    public void testKeystoreExists() {
        File keystoreFile = new File(KEYSTORE_PATH);
        File truststoreFile = new File(TRUSTSTORE_PATH);
        
        assertTrue("Keystore file should exist", keystoreFile.exists());
        assertTrue("Truststore file should exist", truststoreFile.exists());
    }

    @Test
    public void testKeystoreContents() throws Exception {
        // Test keystore
        KeyStore keystore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keystore.load(fis, STORE_PASSWORD.toCharArray());
            assertTrue("Keystore should contain test certificate", keystore.containsAlias(CERT_ALIAS));
            assertTrue("Keystore should contain private key", keystore.isKeyEntry(CERT_ALIAS));
        }

        // Test truststore
        KeyStore truststore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(TRUSTSTORE_PATH)) {
            truststore.load(fis, STORE_PASSWORD.toCharArray());
            assertTrue("Truststore should contain test certificate", truststore.containsAlias(CERT_ALIAS));
            assertTrue("Truststore should contain certificate", truststore.isCertificateEntry(CERT_ALIAS));
        }
    }

    @Test
    public void testCertificateValidity() throws Exception {
        KeyStore truststore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(TRUSTSTORE_PATH)) {
            truststore.load(fis, STORE_PASSWORD.toCharArray());
            X509Certificate cert = (X509Certificate) truststore.getCertificate(CERT_ALIAS);
            
            assertNotNull("Certificate should not be null", cert);
            
            // Verify certificate is currently valid
            cert.checkValidity();
            
            // Verify basic certificate attributes
            assertNotNull("Subject DN should not be null", cert.getSubjectDN());
            assertNotNull("Issuer DN should not be null", cert.getIssuerDN());
            assertTrue("Certificate should be valid for 365 days", 
                cert.getNotAfter().getTime() - cert.getNotBefore().getTime() >= 365 * 24 * 60 * 60 * 1000L);
        }
    }

    @Test
    public void testKeystoreTruststoreCompatibility() throws Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");
        KeyStore truststore = KeyStore.getInstance("JKS");
        
        try (FileInputStream keystoreFis = new FileInputStream(KEYSTORE_PATH);
             FileInputStream truststoreFis = new FileInputStream(TRUSTSTORE_PATH)) {
            
            keystore.load(keystoreFis, STORE_PASSWORD.toCharArray());
            truststore.load(truststoreFis, STORE_PASSWORD.toCharArray());
            
            // Get certificate from keystore
            X509Certificate keystoreCert = (X509Certificate) keystore.getCertificate(CERT_ALIAS);
            // Get certificate from truststore
            X509Certificate truststoreCert = (X509Certificate) truststore.getCertificate(CERT_ALIAS);
            
            assertNotNull("Keystore certificate should not be null", keystoreCert);
            assertNotNull("Truststore certificate should not be null", truststoreCert);
            
            // Verify certificates are identical
            assertEquals("Certificates in keystore and truststore should match",
                keystoreCert.getSerialNumber(), truststoreCert.getSerialNumber());
        }
    }
}
