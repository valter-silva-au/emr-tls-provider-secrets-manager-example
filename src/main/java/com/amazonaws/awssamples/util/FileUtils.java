package com.amazonaws.awssamples.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Creates a directory if it doesn't exist
     * @param directoryPath path to create
     */
    public static void createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                logger.warn("Failed to create directory: {}", directoryPath);
            }
        }
    }

    /**
     * Writes content to a file
     * @param filePath path to write to
     * @param content content to write
     * @throws IOException if writing fails
     */
    public static void writeToFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    /**
     * Writes certificate content to a file, creating parent directories if needed
     * @param filePath path to write to
     * @param certificateContent certificate content to write
     * @throws IOException if writing fails
     */
    public static void writeCertificate(String filePath, String certificateContent) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            createDirectory(parentDir.getPath());
        }
        
        writeToFile(filePath, certificateContent);
        logger.debug("Certificate written to: {}", filePath);
    }
}
