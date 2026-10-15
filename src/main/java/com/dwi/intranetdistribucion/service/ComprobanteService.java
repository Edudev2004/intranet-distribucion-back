package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.ComprobanteRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ComprobantePdfDTO;
import com.dwi.intranetdistribucion.dto.response.ComprobanteResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoComprobanteResponseDTO;

import java.util.List;

public interface ComprobanteService {

    ComprobanteResponseDTO emitir(ComprobanteRequestDTO comprobanteRequestDTO);

    List<ComprobanteResponseDTO> listar(Long idPedido, Short idTipoComprobante);

    ComprobanteResponseDTO obtenerPorId(Long id);

    ComprobantePdfDTO generarPdf(Long id);

    List<TipoComprobanteResponseDTO> listarTipos();
}
