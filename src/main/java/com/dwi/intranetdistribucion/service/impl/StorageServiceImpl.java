package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final S3Client s3Client;

    @Override
    public String uploadFile(MultipartFile file, String bucket) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo a subir no puede estar vacío");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            extension = ".jpg";
        }

        String key = UUID.randomUUID() + extension;
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Archivo subido con éxito a bucket '{}', key '{}'", bucket, key);

            return String.format("https://aejheugcxtmeuuyfkpjo.supabase.co/storage/v1/object/public/%s/%s", bucket, key);
        } catch (IOException e) {
            log.error("Error de E/S al leer archivo para subida a S3: {}", e.getMessage());
            throw new RuntimeException("Error al procesar el archivo para subida a S3", e);
        } catch (Exception e) {
            log.error("Error al subir archivo a Supabase S3: {}", e.getMessage(), e);
            throw new RuntimeException("Error al comunicarse con Supabase Storage S3: " + e.getMessage(), e);
        }
    }
}
