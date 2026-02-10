package com.snnsoluciones.nathbitbusinesscore.controller;

import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.*;
import com.snnsoluciones.nathbitbusinesscore.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para Productos.
 * 
 * Base URL: /api/business/productos
 * 
 * Requiere:
 * - Device Token (X-Device-Token header) para acceder al tenant
 * - Bearer Token para auditoría
 */
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "Gestión de productos del tenant")
public class ProductoController {

    private final ProductoService productoService;

    // ==================== BÚSQUEDA AVANZADA ====================

    /**
     * POST /api/business/productos/buscar
     * Búsqueda avanzada con filtros combinables
     * 
     * FILTROS DISPONIBLES:
     * - codigo: Busca en codigo_interno O codigo_barras
     * - nombre: Búsqueda parcial case-insensitive
     * - precio: Busca considerando impuestos (ej: 113 encuentra precio=100 + IVA 13%)
     * - tipo: VENTA, MATERIA_PRIMA, MIXTO, COMBO, COMPUESTO
     * - zona: NINGUNA, COCINA, BAR, etc
     * - codigoTarifaImpuesto: TARIFA_GENERAL_13, TARIFA_EXENTA, etc
     * - requiereReceta: true/false
     * - requiereInventario: true/false
     * - categoriaId: ID de categoría
     * - activo: true/false (default: true)
     * 
     * PAGINACIÓN:
     * - page: Número de página (0-based, default: 0)
     * - size: Tamaño de página (default: 15)
     * - sortBy: Campo para ordenar (default: nombre)
     * - sortDir: Dirección (asc/desc, default: asc)
     */
    @PostMapping("/buscar")
    @Operation(
        summary = "Búsqueda avanzada de productos",
        description = "Permite combinar múltiples filtros para búsqueda flexible. " +
                      "Soporta búsqueda por precio considerando impuestos aplicados."
    )
    public ResponseEntity<Page<ProductoListDTO>> buscarProductos(
            @Valid @RequestBody ProductoSearchDTO filtros) {
        
        log.info("POST /productos/buscar - Búsqueda con filtros: {}", filtros);
        
        try {
            Page<ProductoListDTO> productos = productoService.buscarProductos(filtros);
            
            log.debug("Se encontraron {} productos", productos.getTotalElements());
            
            return ResponseEntity.ok(productos);
            
        } catch (Exception e) {
            log.error("Error en búsqueda de productos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== LISTADOS SIMPLES ====================

    /**
     * GET /api/business/productos/activos
     * Obtener todos los productos activos (paginado)
     */
    @GetMapping("/activos")
    @Operation(summary = "Listar productos activos")
    public ResponseEntity<Page<ProductoListDTO>> obtenerProductosActivos(
            @Parameter(description = "Número de página (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Tamaño de página") 
            @RequestParam(defaultValue = "15") int size,
            
            @Parameter(description = "Campo para ordenar") 
            @RequestParam(defaultValue = "nombre") String sortBy,
            
            @Parameter(description = "Dirección de ordenamiento (asc/desc)") 
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /productos/activos - page: {}, size: {}", page, size);
        
        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<ProductoListDTO> productos = productoService.obtenerProductosActivos(pageable);
            
            return ResponseEntity.ok(productos);
            
        } catch (Exception e) {
            log.error("Error obteniendo productos activos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/business/productos/categoria/{categoriaId}
     * Obtener productos de una categoría específica
     */
    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar productos por categoría")
    public ResponseEntity<Page<ProductoListDTO>> obtenerProductosPorCategoria(
            @PathVariable Long categoriaId,
            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /productos/categoria/{} - page: {}, size: {}", categoriaId, page, size);
        
        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<ProductoListDTO> productos = productoService.buscarPorCategoria(categoriaId, pageable);
            
            return ResponseEntity.ok(productos);
            
        } catch (Exception e) {
            log.error("Error obteniendo productos por categoría: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== BÚSQUEDA RÁPIDA ====================

    /**
     * GET /api/business/productos/buscar-rapido?termino=xxx
     * Búsqueda rápida por nombre o código (sin paginación)
     * Mínimo 3 caracteres
     */
    @GetMapping("/buscar-rapido")
    @Operation(
        summary = "Búsqueda rápida por nombre o código",
        description = "Busca en nombre, código interno y código de barras. Requiere mínimo 3 caracteres."
    )
    public ResponseEntity<?> buscarRapido(
            @Parameter(description = "Término de búsqueda (mínimo 3 caracteres)") 
            @RequestParam String termino) {
        
        log.info("GET /productos/buscar-rapido?termino={}", termino);
        
        try {
            List<ProductoListDTO> productos = productoService.buscarPorNombreOCodigo(termino);
            
            return ResponseEntity.ok(productos);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validación fallida: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            log.error("Error en búsqueda rápida: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/business/productos/para-inventario?termino=xxx
     * Búsqueda de productos para asignar inventario inicial
     * 
     * FILTROS AUTOMÁTICOS:
     * - requiereInventario = true
     * - tipoInventario = SIMPLE
     * - tipo IN (VENTA, MIXTO, MATERIA_PRIMA)
     * - activo = true
     */
    @GetMapping("/para-inventario")
    @Operation(
        summary = "Buscar productos para inventario",
        description = "Filtra automáticamente productos que requieren control de inventario"
    )
    public ResponseEntity<?> buscarParaInventario(
            @Parameter(description = "Término de búsqueda (mínimo 3 caracteres)") 
            @RequestParam String termino,
            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /productos/para-inventario?termino={}", termino);
        
        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<ProductoListDTO> productos = productoService.buscarParaInventario(termino, pageable);
            
            return ResponseEntity.ok(productos);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validación fallida: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            log.error("Error buscando productos para inventario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CRUD ====================

    /**
     * GET /api/business/productos/{id}
     * Obtener un producto por ID (con relaciones completas)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<?> obtenerProductoPorId(@PathVariable Long id) {
        log.info("GET /productos/{}", id);
        
        try {
            ProductoDTO producto = productoService.obtenerProductoPorId(id);
            return ResponseEntity.ok(producto);
            
        } catch (IllegalArgumentException e) {
            log.warn("Producto no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error obteniendo producto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/business/productos
     * Crear un nuevo producto
     */
    @PostMapping
    @Operation(summary = "Crear producto")
    public ResponseEntity<?> crearProducto(@Valid @RequestBody CreateProductoDTO dto) {
        log.info("POST /productos - Creando producto: {}", dto.getNombre());
        
        try {
            ProductoDTO producto = productoService.crearProducto(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(producto);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            log.error("Error creando producto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/business/productos/{id}
     * Actualizar un producto existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductoDTO dto) {
        
        log.info("PUT /productos/{} - Actualizando producto", id);
        
        try {
            ProductoDTO producto = productoService.actualizarProducto(id, dto);
            return ResponseEntity.ok(producto);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            log.error("Error actualizando producto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PATCH /api/business/productos/{id}/estado
     * Activar/Desactivar un producto
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del producto")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @Parameter(description = "true para activar, false para desactivar") 
            @RequestParam Boolean activo) {
        
        log.info("PATCH /productos/{}/estado?activo={}", id, activo);
        
        try {
            ProductoDTO producto = productoService.cambiarEstadoProducto(id, activo);
            return ResponseEntity.ok(producto);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error cambiando estado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== VALIDACIONES ====================

    /**
     * GET /api/business/productos/validar-codigo?codigo=xxx&productoId=yyy
     * Validar si un código está disponible
     */
    @GetMapping("/validar-codigo")
    @Operation(
        summary = "Validar disponibilidad de código",
        description = "Verifica si un código interno está disponible. " +
                      "Si se proporciona productoId, permite que el producto use su propio código."
    )
    public ResponseEntity<Boolean> validarCodigo(
            @RequestParam String codigo,
            @RequestParam(required = false) Long productoId) {
        
        log.info("GET /productos/validar-codigo?codigo={}&productoId={}", codigo, productoId);
        
        try {
            boolean disponible = productoService.validarCodigoDisponible(codigo, productoId);
            return ResponseEntity.ok(disponible);
            
        } catch (Exception e) {
            log.error("Error validando código: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/business/productos
     * Listar productos con paginación simple (sin filtros)
     */
    @GetMapping
    @Operation(summary = "Listar todos los productos activos")
    public ResponseEntity<Page<ProductoListDTO>> listarProductos(
        @Parameter(description = "Número de página (0-based)")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Tamaño de página")
        @RequestParam(defaultValue = "15") int size,

        @Parameter(description = "Campo para ordenar")
        @RequestParam(defaultValue = "nombre") String sortBy,

        @Parameter(description = "Dirección de ordenamiento (asc/desc)")
        @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /productos - page: {}, size: {}", page, size);

        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<ProductoListDTO> productos = productoService.obtenerProductosActivos(pageable);

            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            log.error("Error listando productos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}