package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.AsignarPedidosRequestDTO;
import com.dwi.intranetdistribucion.dto.request.ProgramacionRutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.AsignacionPedidosResponseDTO;
import com.dwi.intranetdistribucion.dto.response.PedidoDisponibleAsignacionDTO;
import com.dwi.intranetdistribucion.dto.response.ProgramacionRutaResponseDTO;
import com.dwi.intranetdistribucion.dto.response.ResumenCargaRutaDTO;

import java.util.List;

public interface ProgramacionRutaService {
    List<ProgramacionRutaResponseDTO> listarTodas();
    ProgramacionRutaResponseDTO registrarAsignacion(ProgramacionRutaRequestDTO request);
    ProgramacionRutaResponseDTO actualizarEstado(Integer idProgramacion, Short idEstado);

    // RF-15: Asignación de pedidos a rutas y control de capacidad
    List<PedidoDisponibleAsignacionDTO> listarPedidosDisponibles(Integer idAlmacen, Integer idZona);
    ResumenCargaRutaDTO obtenerResumenCarga(Integer idProgramacion);
    AsignacionPedidosResponseDTO asignarPedidosARuta(Integer idProgramacion, AsignarPedidosRequestDTO request);
    AsignacionPedidosResponseDTO autoAsignarPedidosARuta(Integer idProgramacion);
    void desasignarPedidoDeRuta(Integer idProgramacion, Long idPedido);
}
