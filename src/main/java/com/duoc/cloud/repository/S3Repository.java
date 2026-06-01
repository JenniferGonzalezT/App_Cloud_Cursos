package com.duoc.cloud.repository;

public interface S3Repository {
    
    void subirArchivo(String folderName, String fileName, byte[] content);

    byte[] descargarArchivo(String folderName, String fileName);
    
    void borrarArchivo(String folderName, String fileName);
}