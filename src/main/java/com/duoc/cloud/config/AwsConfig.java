package com.duoc.cloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${APP_AWS_ACCESS_KEY_ID:}")
    private String accessKey;

    @Value("${APP_AWS_SECRET_ACCESS_KEY:}")
    private String secretKey;

    @Value("${APP_AWS_SESSION_TOKEN:}")
    private String sessionToken;

    @Bean
    public S3Client s3Client() {
        // Validamos si estamos usando tokens temporales de AWS Academy (requieren Session Token)
        if (sessionToken != null && !sessionToken.trim().isEmpty()) {
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsSessionCredentials.create(accessKey, secretKey, sessionToken)
                    ))
                    .build();
        } else {
            // Si pruebas local sin session token (credenciales estándar fijas)
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .build();
        }
    }
}