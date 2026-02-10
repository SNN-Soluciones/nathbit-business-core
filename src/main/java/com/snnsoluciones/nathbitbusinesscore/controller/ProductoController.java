package com.snnsoluciones.nathbitbusinesscore.controller;

import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.*;
import com.snnsoluciones.nathbitbusinesscore.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ==================== CREAR ====================

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody CreateProductoDTO dto) {
        log.info("POST /api/productos - {}", dto.getCodigoInterno());
        ProductoDTO producto = productoService.crearProducto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @PostMapping(value = "/con-imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoDTO> crearConImagen(
            @RequestPart("producto") @Valid CreateProductoDTO dto,
            @RequestPart("imagen") MultipartFile imagen) {
        log.info("POST /api/productos/con-imagen - {}", dto.getCodigoInterno());
        ProductoDTO producto = productoService.crearProductoConImagen(dto, imagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    // ==================== ACTUALIZAR ====================

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductoDTO dto) {
        log.info("PUT /api/productos/{}", id);
        ProductoDTO producto = productoService.actualizarProducto(id, dto);
        return ResponseEntity.ok(producto);
    }

    @PutMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoDTO> actualizarImagen(
            @PathVariable Long id,
            @RequestPart("imagen") MultipartFile imagen) {
        log.info("PUT /api/productos/{}/imagen", id);
        ProductoDTO producto = productoService.actualizarImagen(id, imagen);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<ProductoDTO> eliminarImagen(@PathVariable Long id) {
        log.info("DELETE /api/productos/{}/imagen", id);
        ProductoDTO producto = productoService.eliminarImagen(id);
        return ResponseEntity.ok(producto);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProductoDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        log.info("PATCH /api/productos/{}/estado?activo={}", id, activo);
        ProductoDTO producto = productoService.cambiarEstado(id, activo);
        return ResponseEntity.ok(producto);
    }

    // ==================== ELIMINAR ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/productos/{}", id);
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CONSULTAS ====================

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/productos/{}", id);
        ProductoDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    @GetMapping("/activos")
    public ResponseEntity<ProductoPage> listarActivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/productos/activos?page={}&size={}", page, size);
        ProductoPage productos = productoService.listarActivos(page, size, sortBy, sortDir);
        return ResponseEntity.ok(productos);
    }

    @GetMapping
    public ResponseEntity<ProductoPage> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/productos?page={}&size={}", page, size);
        ProductoPage productos = productoService.listarTodos(page, size);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/buscar-rapido")
    public ResponseEntity<List<ProductoListDTO>> buscarRapido(@RequestParam String termino) {
        log.info("GET /api/productos/buscar-rapido?termino={}", termino);
        List<ProductoListDTO> productos = productoService.buscarRapido(termino);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/validar-codigo")
    public ResponseEntity<Boolean> validarCodigo(
            @RequestParam String codigo,
            @RequestParam(required = false) Long productoId) {
        log.info("GET /api/productos/validar-codigo?codigo={}", codigo);
        boolean disponible = productoService.validarCodigo(codigo, productoId);
        return ResponseEntity.ok(disponible);
    }
}