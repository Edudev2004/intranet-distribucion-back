package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.ClienteRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteOpcionDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoDocumentoDTO;

import java.util.List;

public interface ClienteService {

    List<ClienteResponseDTO> listarTodos(String filtro, Boolean soloActivos);

    ClienteResponseDTO obtenerPorId(Integer idCliente);

    ClienteResponseDTO crear(ClienteRequestDTO request);

    ClienteResponseDTO actualizar(Integer idCliente, ClienteRequestDTO request);

    void cambiarEstado(Integer idCliente, Boolean activo);

    void eliminar(Integer idCliente);

    List<ClienteOpcionDTO> listarOpciones();

    List<TipoDocumentoDTO> listarTiposDocumento();
}
