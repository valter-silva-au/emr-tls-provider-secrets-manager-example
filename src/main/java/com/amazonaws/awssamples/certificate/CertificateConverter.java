package com.amazonaws.awssamples.certificate;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CertificateConverter {
    
    /**
     * Converts a PEM formatted string to X509 certificates
     */
    public List<Certificate> getX509FromString(String certificateString) throws CertificateConversionException {
        List<Certificate> certs = new ArrayList<>();
        try {
            String[] lines = certificateString.split("\n");
            StringBuilder currCertSb = new StringBuilder();
            for (String line : lines) {
                line = line.trim();
                if (line.equals("-----BEGIN CERTIFICATE-----")) {
                    currCertSb.setLength(0);
                } else if (line.equals("-----END CERTIFICATE-----")) {
                    byte[] certificateData = Base64.getDecoder()
                            .decode(currCertSb.toString().replaceAll("\\s+", ""));

                    CertificateFactory cf = CertificateFactory.getInstance("X509");
                    certs.add(cf.generateCertificate(new ByteArrayInputStream(certificateData)));
                } else {
                    currCertSb.append(line).append("\n");
                }
            }
            return certs;
        } catch (CertificateException e) {
            throw new CertificateConversionException("Failed to convert certificate string to X509", e);
        }
    }

    /**
     * Converts a PEM formatted private key string to a PrivateKey object
     */
    public PrivateKey getPrivateKey(String pkey) throws PrivateKeyGenerationException {
        try {
            pkey = pkey.replace("-----BEGIN PRIVATE KEY-----", "")
                      .replace("-----END PRIVATE KEY-----", "")
                      .replaceAll("\\s+", "");

            byte[] pkeyEncodedBytes = Base64.getDecoder().decode(pkey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkeyEncodedBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PrivateKeyGenerationException("Failed to generate private key", e);
        }
    }
}
