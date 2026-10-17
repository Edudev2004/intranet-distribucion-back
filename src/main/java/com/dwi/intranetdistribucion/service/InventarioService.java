package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.AjusteStockRequestDTO;
import com.dwi.intranetdistribucion.dto.request.MovimientoRequestDTO;
import com.dwi.intranetdistribucion.dto.response.*;

import java.time.OffsetDateTime;
import java.util.List;

public interface InventarioService {

    InventarioConsultaDTO consultarInventario(Integer idAlmacen, Integer idCategoria, Boolean soloBajoStock, String buscar);

    List<AlertaStockDTO> obtenerAlertas(Integer idAlmacen, Integer idCategoria);

    InventarioItemDTO obtenerStockProducto(Integer idAlmacen, Integer idProducto);

    MovimientoResponseDTO registrarMovimiento(MovimientoRequestDTO request, String username);

    MovimientoResponseDTO ajustarStock(AjusteStockRequestDTO request, String username);

    List<TipoMovimientoResponseDTO> obtenerTiposMovimiento();

    List<MovimientoResponseDTO> obtenerHistorialMovimientos(Integer idAlmacen,
                                                           Integer idProducto,
                                                           Short idTipoMovimiento,
                                                           OffsetDateTime fechaInicio,
                                                           OffsetDateTime fechaFin);
}
