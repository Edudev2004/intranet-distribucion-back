package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ProgramacionRutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ProgramacionRutaResponseDTO;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.ProgramacionRuta;
import com.dwi.intranetdistribucion.repository.ProgramacionRutaRepository;
import com.dwi.intranetdistribucion.service.ProgramacionRutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramacionRutaServiceImpl implements ProgramacionRutaService {

    private final ProgramacionRutaRepository programacionRutaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProgramacionRutaResponseDTO> listarTodas() {
        return programacionRutaRepository.findAll().stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public ProgramacionRutaResponseDTO registrarAsignacion(ProgramacionRutaRequestDTO request) {
        ProgramacionRuta programacion = ProgramacionRuta.builder()
                .idRuta(request.getIdRuta())
                .idVehiculo(request.getIdVehiculo())
                .idConductor(request.getIdConductor())
                .inicioProgramado(request.getInicioProgramado())
                .finProgramado(request.getFinProgramado())
                .idEstadoProgramacion((short) 1) // Estado inicial por defecto (ej. Registrado / Programado)
                .build();

        programacion = programacionRutaRepository.save(programacion);
        return mapearRespuesta(programacion);
    }

    @Override
    @Transactional
    public ProgramacionRutaResponseDTO actualizarEstado(Integer idProgramacion, Short idEstado) {
        ProgramacionRuta programacion = programacionRutaRepository.findById(idProgramacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de ruta no encontrada con id: " + idProgramacion));

        programacion.setIdEstadoProgramacion(idEstado);
        programacion = programacionRutaRepository.save(programacion);
        return mapearRespuesta(programacion);
    }

    private ProgramacionRutaResponseDTO mapearRespuesta(ProgramacionRuta prog) {
        String nombreEstado = programacionRutaRepository.obtenerNombreEstadoProgramacion(prog.getIdEstadoProgramacion().intValue())
                .orElse("Desconocido");

        return ProgramacionRutaResponseDTO.builder()
                .idProgramacion(prog.getIdProgramacion())
                .idRuta(prog.getIdRuta())
                .idVehiculo(prog.getIdVehiculo())
                .idConductor(prog.getIdConductor())
                .inicioProgramado(prog.getInicioProgramado())
                .finProgramado(prog.getFinProgramado())
                .inicioReal(prog.getInicioReal())
                .finReal(prog.getFinReal())
                .idEstadoProgramacion(prog.getIdEstadoProgramacion())
                .estadoProgramacion(nombreEstado)
                .build();
    }
}