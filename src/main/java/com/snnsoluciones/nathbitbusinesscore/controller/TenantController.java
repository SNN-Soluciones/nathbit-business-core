package com.snnsoluciones.nathbitbusinesscore.controller;

import com.snnsoluciones.nathbitbusinesscore.model.dto.tenant.*;
import com.snnsoluciones.nathbitbusinesscore.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    // ==================== TENANT INFO ====================

    @GetMapping("/info")
    public ResponseEntity<TenantInfoDTO> obtenerInfo() {
        log.info("GET /api/tenant/info");
        TenantInfoDTO dto = tenantService.obtenerInfo();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/info")
    public ResponseEntity<TenantInfoDTO> actualizarInfo(
        @Valid @RequestBody UpdateTenantInfoDTO dto) {
        log.info("PUT /api/tenant/info");
        TenantInfoDTO actualizado = tenantService.actualizarInfo(dto);
        return ResponseEntity.ok(actualizado);
    }

    // ==================== TENANT CONFIGURACION ====================

    @GetMapping("/configuracion")
    public ResponseEntity<TenantConfiguracionDTO> obtenerConfiguracion() {
        log.info("GET /api/tenant/configuracion");
        TenantConfiguracionDTO dto = tenantService.obtenerConfiguracion();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/configuracion")
    public ResponseEntity<TenantConfiguracionDTO> actualizarConfiguracion(
        @Valid @RequestBody UpdateTenantConfiguracionDTO dto) {
        log.info("PUT /api/tenant/configuracion");
        TenantConfiguracionDTO actualizado = tenantService.actualizarConfiguracion(dto);
        return ResponseEntity.ok(actualizado);
    }
}