package com.snnsoluciones.nathbitbusinesscore.controller;

import com.snnsoluciones.nathbitbusinesscore.model.dto.CodigoCabysDTO;
import com.snnsoluciones.nathbitbusinesscore.service.CodigoCabysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para buscar en el catálogo global de códigos CAByS.
 * 
 * Base URL: /api/business/codigos-cabys
 * 
 * NOTA: Esta tabla está en PUBLIC (no usa multi-tenancy),
 * pero sigue requiriendo X-Device-Token para autenticación.
 */
@RestController
@RequestMapping("/codigos-cabys")
@RequiredArgsConstructor
@Slf4j
public class CodigoCabysController {

    private final CodigoCabysService codigoCabysService;

    /**
     * GET /api/business/codigos-cabys/buscar
     * Buscar códigos CAByS con filtros opcionales
     * 
     * Query params:
     * - descripcion: Filtro parcial case-insensitive (ej: "cafe")
     * - codigo: Filtro por prefijo (ej: "6332")
     * - impuesto: Filtro exacto (ej: "13", "4", "Exento")
     * 
     * Ejemplo:
     * GET /codigos-cabys/buscar?descripcion=cafe&impuesto=13
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<CodigoCabysDTO>> buscarCabys(
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String impuesto) {
        
        log.info("GET /codigos-cabys/buscar - Filtros: descripcion={}, codigo={}, impuesto={}", 
                 descripcion, codigo, impuesto);

        List<CodigoCabysDTO> resultados = codigoCabysService.buscarCabys(descripcion, codigo, impuesto);

        return ResponseEntity.ok(resultados);
    }
}