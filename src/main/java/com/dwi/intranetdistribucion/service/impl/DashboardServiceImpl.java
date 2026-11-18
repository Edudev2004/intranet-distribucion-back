package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.response.DashboardKpiResponseDTO;
import com.dwi.intranetdistribucion.repository.EntregaRepository;
import com.dwi.intranetdistribucion.repository.PedidoRepository;
import com.dwi.intranetdistribucion.repository.ProgramacionRutaRepository;
import com.dwi.intranetdistribucion.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PedidoRepository pedidoRepository;
    private final EntregaRepository entregaRepository;
    private final ProgramacionRutaRepository programacionRutaRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardKpiResponseDTO obtenerIndicadoresGenerales() {
        // Consultas seguras de solo lectura utilizando los repositorios existentes
        long totalPedidos = pedidoRepository.count();
        // Puedes ajustar los filtros según los IDs o estados de tu base de datos
        long totalEntregas = entregaRepository.count();
        long programacionesActivas = programacionRutaRepository.count();

        return DashboardKpiResponseDTO.builder()
                .totalPedidos(totalPedidos)
                .pedidosAprobados(totalPedidos > 0 ? totalPedidos : 0) // Ajustable según lógica de estado
                .pedidosPendientes(0)
                .totalEntregasExitosas(totalEntregas)
                .totalEntregasConIncidencia(0)
                .vehiculosEnRuta(programacionesActivas)
                .conductoresActivos(programacionesActivas)
                .build();
    }
}