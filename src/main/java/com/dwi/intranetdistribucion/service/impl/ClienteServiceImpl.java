package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.ClienteRequestDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteOpcionDTO;
import com.dwi.intranetdistribucion.dto.response.ClienteResponseDTO;
import com.dwi.intranetdistribucion.dto.response.TipoDocumentoDTO;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.Cliente;
import com.dwi.intranetdistribucion.model.TipoDocumento;
import com.dwi.intranetdistribucion.model.Zona;
import com.dwi.intranetdistribucion.repository.ClienteRepository;
import com.dwi.intranetdistribucion.repository.TipoDocumentoRepository;
import com.dwi.intranetdistribucion.repository.ZonaRepository;
import com.dwi.intranetdistribucion.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ZonaRepository zonaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos(String filtro, Boolean soloActivos) {
        boolean filtrarSoloActivos = soloActivos == null || soloActivos;
        String busqueda = (filtro != null && !filtro.trim().isBlank()) ? filtro.trim() : null;

        List<Cliente> clientes;
        if (busqueda != null) {
            clientes = clienteRepository.buscarConFiltro(busqueda, filtrarSoloActivos);
        } else if (filtrarSoloActivos) {
            clientes = clienteRepository.findByActivoTrueOrderByNombreRazonSocialAsc();
        } else {
            clientes = clienteRepository.findAllByOrderByNombreRazonSocialAsc();
        }

        return clientes.stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Integer idCliente) {
        Cliente cliente = buscarCliente(idCliente);
        return mapToDTO(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO request) {
        String numeroDocumento = request.getNumeroDocumento().trim();
        if (clienteRepository.existsByNumeroDocumento(numeroDocumento)) {
            throw new DuplicateResourceException("Ya existe un cliente registrado con el documento: " + numeroDocumento);
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.getIdTipoDocumento())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado con ID: " + request.getIdTipoDocumento()));

        Zona zona = null;
        if (request.getIdZona() != null) {
            zona = zonaRepository.findById(request.getIdZona())
                    .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + request.getIdZona()));
        }

        Cliente cliente = Cliente.builder()
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .nombreRazonSocial(request.getNombreRazonSocial().trim())
                .direccion(request.getDireccion().trim())
                .telefono(request.getTelefono() != null && !request.getTelefono().isBlank() ? request.getTelefono().trim() : null)
                .correo(request.getCorreo() != null && !request.getCorreo().isBlank() ? request.getCorreo().trim() : null)
                .zona(zona)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Integer idCliente, ClienteRequestDTO request) {
        Cliente cliente = buscarCliente(idCliente);

        String numeroDocumento = request.getNumeroDocumento().trim();
        if (clienteRepository.existsByNumeroDocumentoAndIdClienteNot(numeroDocumento, idCliente)) {
            throw new DuplicateResourceException("Ya existe otro cliente registrado con el documento: " + numeroDocumento);
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.getIdTipoDocumento())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado con ID: " + request.getIdTipoDocumento()));

        Zona zona = null;
        if (request.getIdZona() != null) {
            zona = zonaRepository.findById(request.getIdZona())
                    .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + request.getIdZona()));
        }

        cliente.setTipoDocumento(tipoDocumento);
        cliente.setNumeroDocumento(numeroDocumento);
        cliente.setNombreRazonSocial(request.getNombreRazonSocial().trim());
        cliente.setDireccion(request.getDireccion().trim());
        cliente.setTelefono(request.getTelefono() != null && !request.getTelefono().isBlank() ? request.getTelefono().trim() : null);
        cliente.setCorreo(request.getCorreo() != null && !request.getCorreo().isBlank() ? request.getCorreo().trim() : null);
        cliente.setZona(zona);
        if (request.getActivo() != null) {
            cliente.setActivo(request.getActivo());
        }

        Cliente actualizado = clienteRepository.save(cliente);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idCliente, Boolean activo) {
        Cliente cliente = buscarCliente(idCliente);
        cliente.setActivo(activo);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Integer idCliente) {
        Cliente cliente = buscarCliente(idCliente);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteOpcionDTO> listarOpciones() {
        return clienteRepository.listarOpciones().stream()
                .map(v -> ClienteOpcionDTO.builder()
                        .idCliente(v.getIdCliente())
                        .nombre(v.getNombre())
                        .documento(v.getDocumento())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoDTO> listarTiposDocumento() {
        return tipoDocumentoRepository.findAllByOrderByNombreAsc().stream()
                .map(td -> TipoDocumentoDTO.builder()
                        .idTipoDocumento(td.getIdTipoDocumento())
                        .nombre(td.getNombre())
                        .build())
                .toList();
    }

    private Cliente buscarCliente(Integer idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + idCliente));
    }

    private ClienteResponseDTO mapToDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .idCliente(cliente.getIdCliente())
                .idTipoDocumento(cliente.getTipoDocumento() != null ? cliente.getTipoDocumento().getIdTipoDocumento() : null)
                .tipoDocumentoNombre(cliente.getTipoDocumento() != null ? cliente.getTipoDocumento().getNombre() : null)
                .numeroDocumento(cliente.getNumeroDocumento())
                .nombreRazonSocial(cliente.getNombreRazonSocial())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .correo(cliente.getCorreo())
                .idZona(cliente.getZona() != null ? cliente.getZona().getIdZona() : null)
                .zonaNombre(cliente.getZona() != null ? cliente.getZona().getNombre() : null)
                .activo(cliente.getActivo())
                .build();
    }
}
