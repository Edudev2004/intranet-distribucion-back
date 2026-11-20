package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bucket", defaultValue = "sedes") String bucket
    ) {
        String url = storageService.uploadFile(file, bucket);
        return ResponseEntity.ok(Map.of(
                "url", url,
                "bucket", bucket,
                "mensaje", "Archivo subido exitosamente a Supabase Storage"
        ));
    }
}
