package com.dwi.intranetdistribucion.repository.projection;

// Datos de cabecera del pedido para el documento (pedido + cliente + almacén + transporte)
public interface PedidoCabeceraView {

    Long getIdPedido();

    String getFechaPedido();

    String getFechaEntregaSolicitada();

    String getObservacion();

    String getEstadoPedido();

    String getTipoDocumentoCliente();

    String getNumeroDocumentoCliente();

    String getCliente();

    String getDireccionCliente();

    String getTelefonoCliente();

    String getCorreoCliente();

    String getAlmacen();

    String getSede();

    String getDireccionSede();

    String getPlacaVehiculo();

    String getConductor();

    String getLicenciaConductor();
}
