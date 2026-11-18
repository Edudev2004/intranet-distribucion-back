package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.response.DashboardKpiResponseDTO;

public interface DashboardService {
    DashboardKpiResponseDTO obtenerIndicadoresGenerales();
}