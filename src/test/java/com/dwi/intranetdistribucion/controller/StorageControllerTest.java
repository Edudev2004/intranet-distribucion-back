package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.GlobalExceptionHandler;
import com.dwi.intranetdistribucion.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StorageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private StorageController storageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Debe subir archivo a bucket exitosamente y retornar URL publica")
    void subirArchivo_Exitoso_Status200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-foto.jpg",
                "image/jpeg",
                "contenido-de-prueba".getBytes()
        );

        String mockUrl = "https://aejheugcxtmeuuyfkpjo.supabase.co/storage/v1/object/public/sedes/uuid-test.jpg";
        when(storageService.uploadFile(any(), eq("sedes"))).thenReturn(mockUrl);

        mockMvc.perform(multipart("/api/v1/storage/upload")
                        .file(file)
                        .param("bucket", "sedes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", is(mockUrl)))
                .andExpect(jsonPath("$.bucket", is("sedes")));
    }

    @Test
    @DisplayName("Debe responder 400 cuando el archivo esta vacio")
    void subirArchivo_Vacio_Status400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vacio.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(storageService.uploadFile(any(), eq("sedes")))
                .thenThrow(new BadRequestException("El archivo a subir no puede estar vacío"));

        mockMvc.perform(multipart("/api/v1/storage/upload")
                        .file(file)
                        .param("bucket", "sedes"))
                .andExpect(status().isBadRequest());
    }
}
