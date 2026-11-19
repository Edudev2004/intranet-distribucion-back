package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.response.DashboardKpiResponseDTO;
import com.dwi.intranetdistribucion.repository.EntregaRepository;
import com.dwi.intranetdistribucion.repository.IncidenciaRepository;
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
    private final IncidenciaRepository incidenciaRepository;
    private final ProgramacionRutaRepository programacionRutaRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardKpiResponseDTO obtenerIndicadoresGenerales() {
        long totalPedidos = pedidoRepository.count();
        long pedidosAprobados = pedidoRepository.countByIdEstadoPedido((short) 2);
        long pedidosPendientes = pedidoRepository.countByIdEstadoPedido((short) 1);

        long totalEntregasExitosas = entregaRepository.countByIdEstadoEntrega((short) 3);
        long totalEntregasConIncidencia = incidenciaRepository.count();
        long programacionesActivas = programacionRutaRepository.count();

        return DashboardKpiResponseDTO.builder()
                .totalPedidos(totalPedidos)
                .pedidosAprobados(pedidosAprobados)
                .pedidosPendientes(pedidosPendientes)
                .totalEntregasExitosas(totalEntregasExitosas)
                .totalEntregasConIncidencia(totalEntregasConIncidencia)
                .vehiculosEnRuta(programacionesActivas)
                .conductoresActivos(programacionesActivas)
                .build();
    }
}
