package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.request.SedeRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ResponsableSedeResponseDTO;
import com.dwi.intranetdistribucion.dto.response.SedeResponseDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.GlobalExceptionHandler;
import com.dwi.intranetdistribucion.exception.ResourceInUseException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.service.SedeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SedeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SedeService sedeService;

    @InjectMocks
    private SedeController sedeController;

    private SedeResponseDTO sedeResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sedeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sedeResponse = SedeResponseDTO.builder()
                .idSede(1)
                .nombre("Sede Central")
                .direccion("Av. España 100, Trujillo")
                .idResponsable(5)
                .nombreResponsable("Ana Mendoza")
                .correoResponsable("amendoza@distribucion.com")
                .activo(true)
                .totalUsuarios(12L)
                .totalAlmacenes(3L)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/sedes retorna lista de sedes con status 200")
    void obtenerTodas_Status200() throws Exception {
        when(sedeService.obtenerTodas(false)).thenReturn(List.of(sedeResponse));

        mockMvc.perform(get("/api/v1/sedes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Sede Central")))
                .andExpect(jsonPath("$[0].totalUsuarios", is(12)));
    }

    @Test
    @DisplayName("GET /api/v1/sedes/responsables retorna responsables disponibles")
    void obtenerResponsablesDisponibles_Status200() throws Exception {
        ResponsableSedeResponseDTO resp = ResponsableSedeResponseDTO.builder()
                .idUsuario(5)
                .nombreCompleto("Ana Mendoza")
                .username("amendoza")
                .correo("amendoza@distribucion.com")
                .rol("ADMIN")
                .build();

        when(sedeService.obtenerResponsablesDisponibles()).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/sedes/responsables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("amendoza")));
    }

    @Test
    @DisplayName("GET /api/v1/sedes/{id} retorna sede con status 200")
    void obtenerPorId_Status200() throws Exception {
        when(sedeService.obtenerPorId(1)).thenReturn(sedeResponse);

        mockMvc.perform(get("/api/v1/sedes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSede", is(1)))
                .andExpect(jsonPath("$.nombre", is("Sede Central")));
    }

    @Test
    @DisplayName("GET /api/v1/sedes/{id} con id inexistente retorna 404")
    void obtenerPorId_NoExiste_Status404() throws Exception {
        when(sedeService.obtenerPorId(99))
                .thenThrow(new ResourceNotFoundException("Sede no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/sedes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/sedes con datos válidos retorna status 201")
    void crear_Valido_Status201() throws Exception {
        String jsonRequest = """
                {
                    "nombre": "Sede Central",
                    "direccion": "Av. España 100, Trujillo",
                    "idResponsable": 5,
                    "activo": true
                }
                """;

        when(sedeService.crear(any(SedeRequestDTO.class))).thenReturn(sedeResponse);

        mockMvc.perform(post("/api/v1/sedes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Sede Central")));
    }

    @Test
    @DisplayName("POST /api/v1/sedes con campos inválidos retorna status 400")
    void crear_Invalido_Status400() throws Exception {
        String jsonInvalido = """
                {
                    "nombre": "",
                    "direccion": ""
                }
                """;

        mockMvc.perform(post("/api/v1/sedes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verify(sedeService, never()).crear(any());
    }

    @Test
    @DisplayName("POST /api/v1/sedes con nombre duplicado retorna status 409")
    void crear_Duplicado_Status409() throws Exception {
        String jsonRequest = """
                {
                    "nombre": "Sede Central",
                    "direccion": "Av. España 100"
                }
                """;

        when(sedeService.crear(any(SedeRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Ya existe una sede con el nombre 'Sede Central'"));

        mockMvc.perform(post("/api/v1/sedes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/sedes/{id} retorna status 200")
    void actualizar_Status200() throws Exception {
        String jsonRequest = """
                {
                    "nombre": "Sede Central Actualizada",
                    "direccion": "Av. España 200",
                    "idResponsable": 5,
                    "activo": true
                }
                """;

        when(sedeService.actualizar(eq(1), any(SedeRequestDTO.class))).thenReturn(sedeResponse);

        mockMvc.perform(put("/api/v1/sedes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/sedes/{id} retorna status 204")
    void eliminar_Status204() throws Exception {
        doNothing().when(sedeService).eliminar(1);

        mockMvc.perform(delete("/api/v1/sedes/1"))
                .andExpect(status().isNoContent());

        verify(sedeService).eliminar(1);
    }

    @Test
    @DisplayName("DELETE /api/v1/sedes/{id} con usuarios o inventario asociado retorna status 409")
    void eliminar_ConDependencias_Status409() throws Exception {
        doThrow(new ResourceInUseException("No se puede eliminar la sede porque tiene usuarios asociados"))
                .when(sedeService).eliminar(1);

        mockMvc.perform(delete("/api/v1/sedes/1"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /api/v1/sedes/{id}/estado actualiza estado activo")
    void cambiarEstado_Status200() throws Exception {
        when(sedeService.cambiarEstado(1, false)).thenReturn(sedeResponse);

        mockMvc.perform(patch("/api/v1/sedes/1/estado")
                        .param("activo", "false"))
                .andExpect(status().isOk());

        verify(sedeService).cambiarEstado(1, false);
    }
}
