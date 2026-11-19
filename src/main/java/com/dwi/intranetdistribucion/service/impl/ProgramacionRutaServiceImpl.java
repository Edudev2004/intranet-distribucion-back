package com.dwi.intranetdistribucion.service.impl;

import com.dwi.intranetdistribucion.dto.request.AsignarPedidosRequestDTO;
import com.dwi.intranetdistribucion.dto.request.ProgramacionRutaRequestDTO;
import com.dwi.intranetdistribucion.dto.response.*;
import com.dwi.intranetdistribucion.exception.BadRequestException;
import com.dwi.intranetdistribucion.exception.DuplicateResourceException;
import com.dwi.intranetdistribucion.exception.ResourceNotFoundException;
import com.dwi.intranetdistribucion.model.*;
import com.dwi.intranetdistribucion.repository.*;
import com.dwi.intranetdistribucion.repository.projection.PedidoDisponibleAsignacionView;
import com.dwi.intranetdistribucion.service.ProgramacionRutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramacionRutaServiceImpl implements ProgramacionRutaService {

    private final ProgramacionRutaRepository programacionRutaRepository;
    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final RutaRepository rutaRepository;
    private final ConductorRepository conductorRepository;

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
                .idEstadoProgramacion((short) 1)
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

    // =========================================================================
    // RF-15: ASIGNACIÓN DE PEDIDOS A RUTAS Y VALIDACIÓN DE CAPACIDAD DE CARGA
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDisponibleAsignacionDTO> listarPedidosDisponibles(Integer idAlmacen, Integer idZona) {
        List<PedidoDisponibleAsignacionView> views = pedidoRepository.listarPedidosDisponiblesParaRuta(idAlmacen, idZona);
        return views.stream().map(v -> PedidoDisponibleAsignacionDTO.builder()
                .idPedido(v.getIdPedido())
                .idCliente(v.getIdCliente())
                .cliente(v.getCliente())
                .numeroDocumentoCliente(v.getNumeroDocumentoCliente())
                .direccionEntrega(v.getDireccionEntrega())
                .zonaCliente(v.getZonaCliente())
                .idAlmacen(v.getIdAlmacen())
                .almacen(v.getAlmacen())
                .idEstadoPedido(v.getIdEstadoPedido())
                .estado(v.getEstado())
                .fechaPedido(v.getFechaPedido())
                .fechaEntregaSolicitada(v.getFechaEntregaSolicitada())
                .pesoTotalKg(v.getPesoTotalKg() != null ? v.getPesoTotalKg() : BigDecimal.ZERO)
                .importeTotal(v.getImporteTotal() != null ? v.getImporteTotal() : BigDecimal.ZERO)
                .build()
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCargaRutaDTO obtenerResumenCarga(Integer idProgramacion) {
        ProgramacionRuta programacion = programacionRutaRepository.findById(idProgramacion)
                .orElseThrow(() -> new ResourceNotFoundException("Programación de ruta no encontrada con ID: " + idProgramacion));

        Vehiculo vehiculo = vehiculoRepository.findById(programacion.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + programacion.getIdVehiculo()));

        BigDecimal capacidadMaxima = vehiculo.getCapacidadKg() != null ? vehiculo.getCapacidadKg() : BigDecimal.ZERO;
        List<Entrega> entregas = entregaRepository.findByIdProgramacion(idProgramacion);
        BigDecimal pesoOcupado = BigDecimal.ZERO;

        for (Entrega entrega : entregas) {
            if (entrega.getIdPedido() != null) {
                pesoOcupado = pesoOcupado.add(calcularPesoPedido(entrega.getIdPedido()));
            }
        }

        BigDecimal pesoDisponible = capacidadMaxima.subtract(pesoOcupado).max(BigDecimal.ZERO);
        BigDecimal porcentaje = capacidadMaxima.compareTo(BigDecimal.ZERO) > 0
                ? pesoOcupado.multiply(BigDecimal.valueOf(100)).divide(capacidadMaxima, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ResumenCargaRutaDTO.builder()
                .idProgramacion(idProgramacion)
                .idVehiculo(vehiculo.getIdVehiculo())
                .placaVehiculo(vehiculo.getPlaca())
                .modeloVehiculo(vehiculo.getMarca() + " " + vehiculo.getModelo())
                .capacidadMaximaKg(capacidadMaxima)
                .pesoOcupadoKg(pesoOcupado)
                .pesoDisponibleKg(pesoDisponible)
                .porcentajeOcupacion(porcentaje)
                .totalPedidos(entregas.size())
                .build();
    }

    @Override
    @Transactional
    public AsignacionPedidosResponseDTO asignarPedidosARuta(Integer idProgramacion, AsignarPedidosRequestDTO request) {
        ProgramacionRuta programacion = programacionRutaRepository.findById(idProgramacion)
                .orElseThrow(() -> new ResourceNotFoundException("Programación de ruta no encontrada con ID: " + idProgramacion));

        Vehiculo vehiculo = vehiculoRepository.findById(programacion.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + programacion.getIdVehiculo()));

        BigDecimal capacidadMaxima = vehiculo.getCapacidadKg() != null ? vehiculo.getCapacidadKg() : BigDecimal.ZERO;

        // Entregas ya programadas en esta ruta
        List<Entrega> entregasExistentes = entregaRepository.findByIdProgramacionOrderByOrdenVisitaAsc(idProgramacion);
        BigDecimal pesoOcupadoActual = BigDecimal.ZERO;
        for (Entrega entrega : entregasExistentes) {
            if (entrega.getIdPedido() != null) {
                pesoOcupadoActual = pesoOcupadoActual.add(calcularPesoPedido(entrega.getIdPedido()));
            }
        }

        short siguienteOrden = (short) (entregasExistentes.size() + 1);
        BigDecimal pesoNuevosPedidos = BigDecimal.ZERO;
        List<Pedido> pedidosParaAsignar = new ArrayList<>();

        for (Long idPedido : request.getIdPedidos()) {
            Pedido pedido = pedidoRepository.findById(idPedido)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + idPedido));

            // CRITERIO DE ACEPTACIÓN 1: Solo se pueden asignar pedidos en estado 'aprobado' (id_estado_pedido = 2)
            if (pedido.getIdEstadoPedido() == null || pedido.getIdEstadoPedido() != 2) {
                String nombreEstado = pedidoRepository.obtenerNombreEstado(pedido.getIdEstadoPedido() != null ? pedido.getIdEstadoPedido().intValue() : -1)
                        .orElse("Desconocido");
                throw new BadRequestException("El pedido #" + idPedido + " no está en estado Aprobado (estado actual: " + nombreEstado + "). Solo se pueden asignar pedidos en estado 'aprobado'.");
            }

            // Validar que no esté ya en esta ruta
            boolean yaEnEstaRuta = entregasExistentes.stream().anyMatch(e -> e.getIdPedido().equals(idPedido));
            if (yaEnEstaRuta) {
                throw new DuplicateResourceException("El pedido #" + idPedido + " ya se encuentra asignado a esta programación de ruta.");
            }

            // Validar que no esté asignado en otra ruta activa
            if (entregaRepository.existsByIdPedidoAndIdEstadoEntregaIn(idPedido, List.of((short) 1, (short) 2, (short) 3))) {
                throw new BadRequestException("El pedido #" + idPedido + " ya se encuentra asignado en otra programación de ruta activa.");
            }

            BigDecimal pesoPedido = calcularPesoPedido(idPedido);
            pesoNuevosPedidos = pesoNuevosPedidos.add(pesoPedido);
            pedidosParaAsignar.add(pedido);
        }

        // CRITERIO DE ACEPTACIÓN 2: El sistema valida que no se exceda la capacidad del vehículo
        BigDecimal pesoTotalProyectado = pesoOcupadoActual.add(pesoNuevosPedidos);
        if (pesoTotalProyectado.compareTo(capacidadMaxima) > 0) {
            BigDecimal exceso = pesoTotalProyectado.subtract(capacidadMaxima);
            throw new BadRequestException(String.format(
                    "Capacidad de carga excedida: El vehículo %s tiene una capacidad máxima de %.2f kg. " +
                    "La carga actual es de %.2f kg y los nuevos pedidos suman %.2f kg (Total requerido: %.2f kg). " +
                    "Se sobrepasa la capacidad por %.2f kg.",
                    vehiculo.getPlaca(), capacidadMaxima, pesoOcupadoActual, pesoNuevosPedidos, pesoTotalProyectado, exceso
            ));
        }

        // Registrar las nuevas entregas
        List<EntregaResponseDTO> entregasCreadas = new ArrayList<>();
        for (Pedido pedido : pedidosParaAsignar) {
            Entrega entrega = Entrega.builder()
                    .idProgramacion(idProgramacion)
                    .idPedido(pedido.getIdPedido())
                    .idEstadoEntrega((short) 1) // Pendiente
                    .ordenVisita(siguienteOrden++)
                    .build();

            Entrega guardada = entregaRepository.save(entrega);
            entregasCreadas.add(mapearEntregaResponse(guardada, pedido));
        }

        BigDecimal pesoTotalFinal = pesoOcupadoActual.add(pesoNuevosPedidos);
        BigDecimal pesoDisponibleFinal = capacidadMaxima.subtract(pesoTotalFinal).max(BigDecimal.ZERO);
        BigDecimal porcentajeFinal = capacidadMaxima.compareTo(BigDecimal.ZERO) > 0
                ? pesoTotalFinal.multiply(BigDecimal.valueOf(100)).divide(capacidadMaxima, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AsignacionPedidosResponseDTO.builder()
                .idProgramacion(idProgramacion)
                .idVehiculo(vehiculo.getIdVehiculo())
                .vehiculoPlaca(vehiculo.getPlaca())
                .capacidadVehiculoKg(capacidadMaxima)
                .pesoTotalAsignadoKg(pesoTotalFinal)
                .pesoDisponibleKg(pesoDisponibleFinal)
                .porcentajeOcupacion(porcentajeFinal)
                .cantidadPedidosAsignados(pedidosParaAsignar.size())
                .mensaje(String.format("Se asignaron exitosamente %d pedido(s) a la ruta. Ocupación: %.1f%% de %.2f kg.",
                        pedidosParaAsignar.size(), porcentajeFinal, capacidadMaxima))
                .entregas(entregasCreadas)
                .build();
    }

    @Override
    @Transactional
    public AsignacionPedidosResponseDTO autoAsignarPedidosARuta(Integer idProgramacion) {
        ProgramacionRuta programacion = programacionRutaRepository.findById(idProgramacion)
                .orElseThrow(() -> new ResourceNotFoundException("Programación de ruta no encontrada con ID: " + idProgramacion));

        Vehiculo vehiculo = vehiculoRepository.findById(programacion.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + programacion.getIdVehiculo()));

        BigDecimal capacidadMaxima = vehiculo.getCapacidadKg() != null ? vehiculo.getCapacidadKg() : BigDecimal.ZERO;

        // Carga actual
        List<Entrega> entregasExistentes = entregaRepository.findByIdProgramacion(idProgramacion);
        BigDecimal pesoOcupadoActual = BigDecimal.ZERO;
        for (Entrega entrega : entregasExistentes) {
            if (entrega.getIdPedido() != null) {
                pesoOcupadoActual = pesoOcupadoActual.add(calcularPesoPedido(entrega.getIdPedido()));
            }
        }

        BigDecimal capacidadDisponible = capacidadMaxima.subtract(pesoOcupadoActual);
        if (capacidadDisponible.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(String.format("La ruta ya ha alcanzado o superado su capacidad máxima (%.2f kg). Capacidad disponible: 0.00 kg.", capacidadMaxima));
        }

        // Obtener la zona de la ruta si existe
        Integer idZona = null;
        if (programacion.getIdRuta() != null) {
            var rutaOpt = rutaRepository.findById(programacion.getIdRuta());
            if (rutaOpt.isPresent() && rutaOpt.get().getZona() != null) {
                idZona = rutaOpt.get().getZona().getIdZona();
            }
        }

        List<PedidoDisponibleAsignacionDTO> disponibles = listarPedidosDisponibles(null, idZona);
        if (disponibles.isEmpty() && idZona != null) {
            // Si no hay pedidos en la zona exacta, buscar pedidos de cualquier zona
            disponibles = listarPedidosDisponibles(null, null);
        }

        if (disponibles.isEmpty()) {
            throw new BadRequestException("No hay pedidos aprobados pendientes de asignación en el sistema.");
        }

        List<Long> pedidosParaAsignar = new ArrayList<>();
        BigDecimal pesoAcumulado = BigDecimal.ZERO;

        for (PedidoDisponibleAsignacionDTO pedido : disponibles) {
            BigDecimal pesoPedido = pedido.getPesoTotalKg() != null ? pedido.getPesoTotalKg() : BigDecimal.ZERO;
            if (pesoAcumulado.add(pesoPedido).compareTo(capacidadDisponible) <= 0) {
                pedidosParaAsignar.add(pedido.getIdPedido());
                pesoAcumulado = pesoAcumulado.add(pesoPedido);
            }
        }

        if (pedidosParaAsignar.isEmpty()) {
            throw new BadRequestException(String.format("Ninguno de los pedidos disponibles cabe en el espacio de carga restante (%.2f kg disponibles).", capacidadDisponible));
        }

        AsignarPedidosRequestDTO req = new AsignarPedidosRequestDTO();
        req.setIdPedidos(pedidosParaAsignar);
        return asignarPedidosARuta(idProgramacion, req);
    }

    @Override
    @Transactional
    public void desasignarPedidoDeRuta(Integer idProgramacion, Long idPedido) {
        Entrega entrega = entregaRepository.findByIdProgramacionAndIdPedido(idProgramacion, idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la entrega para el pedido #" + idPedido + " en la ruta #" + idProgramacion));

        if (entrega.getIdEstadoEntrega() != null && entrega.getIdEstadoEntrega() != 1) {
            throw new BadRequestException("Solo se pueden desasignar entregas en estado Pendiente.");
        }

        entregaRepository.delete(entrega);
    }

    private BigDecimal calcularPesoPedido(Long idPedido) {
        List<DetallePedido> detalles = detallePedidoRepository.findByIdPedido(idPedido);
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido dp : detalles) {
            BigDecimal cantidad = dp.getCantidad() != null ? dp.getCantidad() : BigDecimal.ZERO;
            BigDecimal pesoUnitario = productoRepository.findById(dp.getIdProducto())
                    .map(Producto::getPesoKg)
                    .filter(p -> p != null)
                    .orElse(BigDecimal.ZERO);
            total = total.add(cantidad.multiply(pesoUnitario));
        }
        return total;
    }

    private EntregaResponseDTO mapearEntregaResponse(Entrega entrega, Pedido pedido) {
        String cliente = null;
        String direccion = null;
        String telefono = null;

        if (pedido != null) {
            try {
                var cabeceraOpt = pedidoRepository.obtenerCabecera(pedido.getIdPedido());
                if (cabeceraOpt != null && cabeceraOpt.isPresent()) {
                    var v = cabeceraOpt.get();
                    cliente = v.getCliente();
                    direccion = v.getDireccionCliente();
                    telefono = v.getTelefonoCliente();
                }
            } catch (Exception ignored) {}
        }

        return EntregaResponseDTO.builder()
                .idEntrega(entrega.getIdEntrega())
                .idProgramacion(entrega.getIdProgramacion())
                .idPedido(entrega.getIdPedido())
                .idEstadoEntrega(entrega.getIdEstadoEntrega())
                .estadoEntrega("Pendiente")
                .ordenVisita(entrega.getOrdenVisita())
                .horaEstimada(entrega.getHoraEstimada())
                .horaEntregaReal(entrega.getHoraEntregaReal())
                .fotoEntregaUrl(entrega.getFotoEntregaUrl())
                .fotoGuiaUrl(entrega.getFotoGuiaUrl())
                .notas(entrega.getNotas())
                .cliente(cliente)
                .direccionCliente(direccion)
                .telefonoCliente(telefono)
                .build();
    }

    private ProgramacionRutaResponseDTO mapearRespuesta(ProgramacionRuta prog) {
        String nombreEstado = "Registrado";
        try {
            var estadoOpt = programacionRutaRepository.obtenerNombreEstadoProgramacion(prog.getIdEstadoProgramacion().intValue());
            if (estadoOpt != null && estadoOpt.isPresent()) {
                nombreEstado = estadoOpt.get();
            }
        } catch (Exception e) {
            nombreEstado = "Activo";
        }

        String nombreRuta = "Ruta #" + prog.getIdRuta();
        String nombreZona = "General";
        try {
            if (prog.getIdRuta() != null) {
                var rutaOpt = rutaRepository.findById(prog.getIdRuta());
                if (rutaOpt.isPresent()) {
                    var r = rutaOpt.get();
                    nombreRuta = r.getNombre();
                    if (r.getZona() != null) {
                        nombreZona = r.getZona().getNombre();
                    }
                }
            }
        } catch (Exception ignored) {}

        String placa = "VEH-" + prog.getIdVehiculo();
        String modelo = "Vehículo de Carga";
        BigDecimal capacidad = BigDecimal.ZERO;
        try {
            if (prog.getIdVehiculo() != null) {
                var vehOpt = vehiculoRepository.findById(prog.getIdVehiculo());
                if (vehOpt.isPresent()) {
                    var v = vehOpt.get();
                    placa = v.getPlaca();
                    modelo = (v.getMarca() != null ? v.getMarca() + " " : "") + (v.getModelo() != null ? v.getModelo() : "");
                    capacidad = v.getCapacidadKg() != null ? v.getCapacidadKg() : BigDecimal.ZERO;
                }
            }
        } catch (Exception ignored) {}

        String conductor = "Conductor #" + prog.getIdConductor();
        String dni = null;
        String licencia = null;
        try {
            if (prog.getIdConductor() != null) {
                var condOpt = conductorRepository.findById(prog.getIdConductor());
                if (condOpt.isPresent()) {
                    var c = condOpt.get();
                    conductor = c.getNombres() + " " + c.getApellidos();
                    dni = c.getDni();
                    licencia = c.getNroLicencia();
                }
            }
        } catch (Exception ignored) {}

        BigDecimal pesoOcupado = BigDecimal.ZERO;
        int totalEntregas = 0;
        try {
            List<Entrega> entregas = entregaRepository.findByIdProgramacion(prog.getIdProgramacionRuta());
            totalEntregas = entregas.size();
            for (Entrega e : entregas) {
                if (e.getIdPedido() != null) {
                    pesoOcupado = pesoOcupado.add(calcularPesoPedido(e.getIdPedido()));
                }
            }
        } catch (Exception ignored) {}

        BigDecimal porcentaje = BigDecimal.ZERO;
        if (capacidad.compareTo(BigDecimal.ZERO) > 0) {
            porcentaje = pesoOcupado.multiply(BigDecimal.valueOf(100)).divide(capacidad, 1, RoundingMode.HALF_UP);
        }

        return ProgramacionRutaResponseDTO.builder()
                .idProgramacion(prog.getIdProgramacionRuta())
                .idRuta(prog.getIdRuta())
                .nombreRuta(nombreRuta)
                .nombreZona(nombreZona)
                .idVehiculo(prog.getIdVehiculo())
                .placaVehiculo(placa)
                .modeloVehiculo(modelo)
                .capacidadVehiculoKg(capacidad)
                .idConductor(prog.getIdConductor())
                .nombreConductor(conductor)
                .dniConductor(dni)
                .licenciaConductor(licencia)
                .inicioProgramado(prog.getInicioProgramado())
                .finProgramado(prog.getFinProgramado())
                .inicioReal(prog.getInicioReal())
                .finReal(prog.getFinReal())
                .idEstadoProgramacion(prog.getIdEstadoProgramacion())
                .estadoProgramacion(nombreEstado)
                .pesoOcupadoKg(pesoOcupado)
                .porcentajeOcupacion(porcentaje)
                .totalPedidos(totalEntregas)
                .build();
    }
}
