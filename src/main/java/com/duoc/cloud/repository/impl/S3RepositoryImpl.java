package com.duoc.cloud.repository.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.duoc.cloud.repository.S3Repository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Repository
public class S3RepositoryImpl implements S3Repository {

    private final S3Client s3Client;
    private final String bucketName;

    // Inyectamos el S3Client que Spring Cloud AWS crea automáticamente y el nombre del bucket
    public S3RepositoryImpl(S3Client s3Client, @Value("${aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void subirArchivo(String folderName, String fileName, byte[] content) {
        // Formato requerido: nombre_carpeta/nombre_archivo (ej: 15/resumen.txt)
        String key = folderName + "/" + fileName;
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("text/plain")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
    }

    @Override
    public byte[] descargarArchivo(String folderName, String fileName) {
        String key = folderName + "/" + fileName;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (S3Exception e) {
            throw new RuntimeException("Error al descargar archivo de S3: " + e.getMessage());
        }
    }

    @Override
    public void borrarArchivo(String folderName, String fileName) {
        String key = folderName + "/" + fileName;

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}