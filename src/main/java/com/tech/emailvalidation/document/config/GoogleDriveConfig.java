package com.tech.emailvalidation.document.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Configures the Google Drive API client using a service-account credentials file.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>Reads the service-account JSON file from the path in
 *       {@code google.drive.credentials-file}.</li>
 *   <li>Scopes it to {@code DriveScopes.DRIVE_FILE} (read/write only
 *       files created or opened by the app).</li>
 *   <li>Produces a singleton {@link Drive} bean used across the application.</li>
 * </ol>
 */
@Configuration
public class GoogleDriveConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveConfig.class);

    @Value("${google.drive.credentials-file}")
    private String credentialsFilePath;

    @Value("${google.drive.application-name:CustomerDocumentService}")
    private String applicationName;

    /**
     * Creates and configures the Google Drive service client.
     *
     * @return configured {@link Drive} instance ready for API calls
     * @throws IOException              if the credentials file cannot be read
     * @throws GeneralSecurityException if HTTP transport setup fails
     */
    @Bean
    public Drive googleDriveClient() throws IOException, GeneralSecurityException {
        log.info("Initializing Google Drive service with credentials from: {}", credentialsFilePath);

        InputStream credentialsStream = new FileInputStream(credentialsFilePath);

        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(credentialsStream)
                .createScoped(Collections.singletonList(DriveScopes.DRIVE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(applicationName)
                .build();
    }
}
