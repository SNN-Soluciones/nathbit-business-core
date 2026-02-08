package com.snnsoluciones.nathbitbusinesscore.controller;

import com.snnsoluciones.nathbitbusinesscore.model.dto.CreateEmpresaCabysDTO;
import com.snnsoluciones.nathbitbusinesscore.model.dto.EmpresaCabysDTO;
import com.snnsoluciones.nathbitbusinesscore.service.EmpresaCabysService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para Códigos CAByS de la Empresa.
 * 
 * Base URL: /api/business/empresa-cabys
 */
@RestController
@RequestMapping("/empresa-cabys")
@RequiredArgsConstructor
@Slf4j
public class EmpresaCabysController {

    private final EmpresaCabysService empresaCabysService;

    /**
     * GET /api/business/empresa-cabys/activos
     * Obtener todos los códigos CAByS activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<EmpresaCabysDTO>> obtenerCabysActivos() {
        log.info("GET /empresa-cabys/activos - Obteniendo CAByS activos");
        
        List<EmpresaCabysDTO> cabys = empresaCabysService.obtenerCabysActivos();
        
        return ResponseEntity.ok(cabys);
    }

    /**
     * GET /api/business/empresa-cabys
     * Obtener todos los códigos CAByS (activos e inactivos)
     */
    @GetMapping
    public ResponseEntity<List<EmpresaCabysDTO>> obtenerTodosCabys() {
        log.info("GET /empresa-cabys - Obteniendo todos los CAByS");
        
        List<EmpresaCabysDTO> cabys = empresaCabysService.obtenerTodosCabys();
        
        return ResponseEntity.ok(cabys);
    }

    /**
     * GET /api/business/empresa-cabys/{id}
     * Obtener un código CAByS por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmpresaCabysDTO> obtenerCabysPorId(@PathVariable Long id) {
        log.info("GET /empresa-cabys/{} - Obteniendo CAByS", id);
        
        EmpresaCabysDTO cabys = empresaCabysService.obtenerCabysPorId(id);
        
        if (cabys == null) {
            log.warn("CAByS con ID {} no encontrado", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(cabys);
    }

    /**
     * POST /api/business/empresa-cabys
     * Crear/Asignar un nuevo código CAByS a la empresa
     */
    @PostMapping
    public ResponseEntity<EmpresaCabysDTO> crearCabys(@Valid @RequestBody CreateEmpresaCabysDTO dto) {
        log.info("POST /empresa-cabys - Creando nuevo CAByS");
        
        try {
            EmpresaCabysDTO cabys = empresaCabysService.crearCabys(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(cabys);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear CAByS: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/business/empresa-cabys/{id}
     * Actualizar un código CAByS
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmpresaCabysDTO> actualizarCabys(
            @PathVariable Long id,
            @Valid @RequestBody CreateEmpresaCabysDTO dto) {
        log.info("PUT /empresa-cabys/{} - Actualizando CAByS", id);
        
        try {
            EmpresaCabysDTO cabys = empresaCabysService.actualizarCabys(id, dto);
            return ResponseEntity.ok(cabys);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar CAByS: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/business/empresa-cabys/{id}/estado
     * Activar/Desactivar un código CAByS
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EmpresaCabysDTO> cambiarEstadoCabys(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        log.info("PATCH /empresa-cabys/{}/estado?activo={} - Cambiando estado", id, activo);
        
        try {
            EmpresaCabysDTO cabys = empresaCabysService.cambiarEstadoCabys(id, activo);
            return ResponseEntity.ok(cabys);
        } catch (IllegalArgumentException e) {
            log.error("Error al cambiar estado de CAByS: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/business/empresa-cabys/{id}
     * Eliminar un código CAByS (soft delete - desactivar)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCabys(@PathVariable Long id) {
        log.info("DELETE /empresa-cabys/{} - Eliminando (desactivando) CAByS", id);
        
        try {
            empresaCabysService.eliminarCabys(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar CAByS: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/business/empresa-cabys/{id}/permanente
     * Eliminar permanentemente un código CAByS
     */
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> eliminarCabysPermanente(@PathVariable Long id) {
        log.warn("DELETE /empresa-cabys/{}/permanente - Eliminando PERMANENTEMENTE CAByS", id);
        
        try {
            empresaCabysService.eliminarCabysPermanente(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar permanentemente CAByS: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}