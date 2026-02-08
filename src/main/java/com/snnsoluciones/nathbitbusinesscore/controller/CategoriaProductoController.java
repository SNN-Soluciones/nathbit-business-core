package com.snnsoluciones.nathbitbusinesscore.controller;

import com.snnsoluciones.nathbitbusinesscore.model.dto.CreateCategoriaProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.dto.CategoriaProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.service.CategoriaProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para Categorías de Productos.
 * 
 * Base URL: /api/business/categorias
 */
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Slf4j
public class CategoriaProductoController {

    private final CategoriaProductoService categoriaProductoService;

    /**
     * GET /api/business/categorias/activas
     * Obtener todas las categorías activas
     * 
     * @return Lista de categorías activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaProductoDTO>> obtenerCategoriasActivas() {
        log.info("GET /categorias/activas - Obteniendo categorías activas");
        
        List<CategoriaProductoDTO> categorias = categoriaProductoService.obtenerCategoriasActivas();
        
        return ResponseEntity.ok(categorias);
    }

    /**
     * GET /api/business/categorias
     * Obtener todas las categorías (activas e inactivas)
     * 
     * @return Lista de todas las categorías
     */
    @GetMapping
    public ResponseEntity<List<CategoriaProductoDTO>> obtenerTodasCategorias() {
        log.info("GET /categorias - Obteniendo todas las categorías");
        
        List<CategoriaProductoDTO> categorias = categoriaProductoService.obtenerTodasCategorias();
        
        return ResponseEntity.ok(categorias);
    }

    /**
     * GET /api/business/categorias/{id}
     * Obtener una categoría por ID
     * 
     * @param id ID de la categoría
     * @return Categoría encontrada o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaProductoDTO> obtenerCategoriaPorId(@PathVariable Long id) {
        log.info("GET /categorias/{} - Obteniendo categoría", id);
        
        CategoriaProductoDTO categoria = categoriaProductoService.obtenerCategoriaPorId(id);
        
        if (categoria == null) {
            log.warn("Categoría con ID {} no encontrada", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(categoria);
    }

    /**
     * POST /api/business/categorias
     * Crear una nueva categoría
     * 
     * @param dto Datos de la categoría
     * @return Categoría creada
     */
    @PostMapping
    public ResponseEntity<CategoriaProductoDTO> crearCategoria(@Valid @RequestBody CreateCategoriaProductoDTO dto) {
        log.info("POST /categorias - Creando nueva categoría");
        
        try {
            CategoriaProductoDTO categoria = categoriaProductoService.crearCategoria(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear categoría: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/business/categorias/{id}
     * Actualizar una categoría
     * 
     * @param id ID de la categoría
     * @param dto Datos actualizados
     * @return Categoría actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaProductoDTO> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoriaProductoDTO dto) {
        log.info("PUT /categorias/{} - Actualizando categoría", id);
        
        try {
            CategoriaProductoDTO categoria = categoriaProductoService.actualizarCategoria(id, dto);
            return ResponseEntity.ok(categoria);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar categoría: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PATCH /api/business/categorias/{id}/estado
     * Activar/Desactivar una categoría
     * 
     * @param id ID de la categoría
     * @param activo true para activar, false para desactivar
     * @return Categoría actualizada
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<CategoriaProductoDTO> cambiarEstadoCategoria(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        log.info("PATCH /categorias/{}/estado?activo={} - Cambiando estado", id, activo);
        
        try {
            CategoriaProductoDTO categoria = categoriaProductoService.cambiarEstadoCategoria(id, activo);
            return ResponseEntity.ok(categoria);
        } catch (IllegalArgumentException e) {
            log.error("Error al cambiar estado de categoría: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/business/categorias/{id}
     * Eliminar una categoría (soft delete - desactivar)
     * 
     * @param id ID de la categoría
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        log.info("DELETE /categorias/{} - Eliminando (desactivando) categoría", id);
        
        try {
            categoriaProductoService.eliminarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar categoría: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/business/categorias/{id}/permanente
     * Eliminar permanentemente una categoría
     * 
     * @param id ID de la categoría
     */
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> eliminarCategoriaPermanente(@PathVariable Long id) {
        log.warn("DELETE /categorias/{}/permanente - Eliminando PERMANENTEMENTE categoría", id);
        
        try {
            categoriaProductoService.eliminarCategoriaPermanente(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar permanentemente categoría: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}