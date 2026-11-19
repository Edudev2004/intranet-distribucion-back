package com.dwi.intranetdistribucion.controller;

import com.dwi.intranetdistribucion.dto.response.DashboardKpiResponseDTO;
import com.dwi.intranetdistribucion.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardKpiResponseDTO> obtenerKpis() {
        return ResponseEntity.ok(dashboardService.obtenerIndicadoresGenerales());
    }
}