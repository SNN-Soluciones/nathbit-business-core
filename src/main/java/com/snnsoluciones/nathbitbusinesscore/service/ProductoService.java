package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.context.TenantContext;
import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.*;
import com.snnsoluciones.nathbitbusinesscore.model.entity.CategoriaProducto;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.model.entity.ProductoImpuesto;
import com.snnsoluciones.nathbitbusinesscore.repository.CategoriaProductoRepository;
import com.snnsoluciones.nathbitbusinesscore.repository.ProductoImpuestoRepository;
import com.snnsoluciones.nathbitbusinesscore.repository.ProductoRepository;
import com.snnsoluciones.nathbitbusinesscore.specification.ProductoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Productos.
 * Requiere tenant configurado en TenantContext.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoImpuestoRepository productoImpuestoRepository;
    private final CategoriaProductoRepository categoriaProductoRepository;

    // ==================== CONSULTAS ====================

    /**
     * Buscar productos con filtros avanzados
     * 
     * @param filtros Criterios de búsqueda
     * @return Página de productos que cumplen los criterios
     */
    @Transactional(readOnly = true)
    public Page<ProductoListDTO> buscarProductos(ProductoSearchDTO filtros) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Buscando productos para tenant: {} con filtros: {}", tenant, filtros);
        
        // Crear specification dinámica
        Specification<Producto> spec = ProductoSpecification.crearBusqueda(filtros);
        
        // Configurar paginación y ordenamiento
        Sort.Direction direction = "desc".equalsIgnoreCase(filtros.getSortDir())
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(
            filtros.getPage(),
            filtros.getSize(),
            Sort.by(direction, filtros.getSortBy())
        );
        
        // Ejecutar búsqueda
        Page<Producto> productos = productoRepository.findAll(spec, pageable);
        
        log.debug("Se encontraron {} productos", productos.getTotalElements());
        
        return productos.map(ProductoListDTO::fromEntity);
    }

    /**
     * Obtener un producto por ID (completo con relaciones)
     * 
     * @param id ID del producto
     * @return DTO completo del producto
     */
    @Transactional(readOnly = true)
    public ProductoDTO obtenerProductoPorId(Long id) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo producto con ID {} para tenant: {}", id, tenant);
        
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
        return ProductoDTO.fromEntity(producto);
    }

    /**
     * Obtener productos activos (listado simple)
     */
    @Transactional(readOnly = true)
    public Page<ProductoListDTO> obtenerProductosActivos(Pageable pageable) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo productos activos para tenant: {}", tenant);
        
        Page<Producto> productos = productoRepository.findByActivoTrue(pageable);
        
        return productos.map(ProductoListDTO::fromEntity);
    }

    /**
     * Buscar productos por nombre o código
     */
    @Transactional(readOnly = true)
    public List<ProductoListDTO> buscarPorNombreOCodigo(String termino) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Buscando productos por término '{}' para tenant: {}", termino, tenant);
        
        if (termino == null || termino.trim().length() < 3) {
            throw new IllegalArgumentException("El término de búsqueda debe tener al menos 3 caracteres");
        }
        
        // Buscar por nombre
        List<Producto> porNombre = productoRepository.buscarPorNombre(termino);
        
        // Buscar por código
        List<Producto> porCodigo = productoRepository.buscarPorCodigo(termino);
        
        // Combinar resultados (sin duplicados)
        Set<Producto> resultados = new HashSet<>();
        resultados.addAll(porNombre);
        resultados.addAll(porCodigo);
        
        log.debug("Se encontraron {} productos", resultados.size());
        
        return resultados.stream()
            .map(ProductoListDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Buscar productos para asignar inventario inicial
     */
    @Transactional(readOnly = true)
    public Page<ProductoListDTO> buscarParaInventario(String termino, Pageable pageable) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Buscando productos para inventario - tenant: {}, término: '{}'", tenant, termino);
        
        if (termino == null || termino.trim().length() < 3) {
            throw new IllegalArgumentException("El término de búsqueda debe tener al menos 3 caracteres");
        }
        
        Page<Producto> productos = productoRepository.buscarParaInventario(termino, pageable);
        
        log.debug("Se encontraron {} productos para inventario", productos.getTotalElements());
        
        return productos.map(ProductoListDTO::fromEntity);
    }

    /**
     * Buscar productos por categoría
     */
    @Transactional(readOnly = true)
    public Page<ProductoListDTO> buscarPorCategoria(Long categoriaId, Pageable pageable) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Buscando productos de categoría {} para tenant: {}", categoriaId, tenant);
        
        Page<Producto> productos = productoRepository.buscarPorCategoria(categoriaId, pageable);
        
        return productos.map(ProductoListDTO::fromEntity);
    }

    // ==================== CREACIÓN ====================

    /**
     * Crear un nuevo producto
     */
    @Transactional
    public ProductoDTO crearProducto(CreateProductoDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Creando producto para tenant: {}", tenant);
        
        // Validar código único
        if (productoRepository.existsByCodigoInterno(dto.getCodigoInterno())) {
            throw new IllegalArgumentException("Ya existe un producto con el código interno: " + dto.getCodigoInterno());
        }
        
        if (dto.getCodigoBarras() != null && productoRepository.existsByCodigoBarras(dto.getCodigoBarras())) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + dto.getCodigoBarras());
        }
        
        // Crear entidad
        Producto producto = Producto.builder()
            .codigoInterno(dto.getCodigoInterno())
            .codigoBarras(dto.getCodigoBarras())
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .empresaCabysId(dto.getEmpresaCabysId())
            .familiaId(dto.getFamiliaId())
            .tipo(dto.getTipo())
            .tipoInventario(dto.getTipoInventario())
            .zonaPreparacion(dto.getZonaPreparacion())
            .precioVenta(dto.getPrecioVenta())
            .precioBase(dto.getPrecioBase())
            .precioCompra(dto.getPrecioCompra())
            .unidadMedida(dto.getUnidadMedida())
            .moneda(dto.getMoneda())
            .unidadMedidaCompra(dto.getUnidadMedidaCompra())
            .unidadMedidaUso(dto.getUnidadMedidaUso())
            .factorConversion(dto.getFactorConversion())
            .factorConversionReceta(dto.getFactorConversionReceta())
            .esServicio(dto.getEsServicio())
            .incluyeIva(dto.getIncluyeIva())
            .requiereInventario(dto.getRequiereInventario())
            .requiereReceta(dto.getRequiereReceta())
            .requierePersonalizacion(dto.getRequierePersonalizacion())
            .activo(true)
            .build();
        
        // Guardar producto
        Producto saved = productoRepository.save(producto);
        log.info("Producto creado con ID: {}", saved.getId());
        
        // Asignar categorías
        if (dto.getCategoriasIds() != null && !dto.getCategoriasIds().isEmpty()) {
            asignarCategorias(saved.getId(), dto.getCategoriasIds());
        }
        
        // Asignar impuestos
        if (dto.getImpuestos() != null && !dto.getImpuestos().isEmpty()) {
            asignarImpuestos(saved.getId(), dto.getImpuestos());
        }
        
        return ProductoDTO.fromEntity(productoRepository.findById(saved.getId()).orElseThrow());
    }

    // ==================== ACTUALIZACIÓN ====================

    /**
     * Actualizar un producto existente
     */
    @Transactional
    public ProductoDTO actualizarProducto(Long id, CreateProductoDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Actualizando producto con ID {} para tenant: {}", id, tenant);
        
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
        // Validar código único (excepto el producto actual)
        if (!producto.getCodigoInterno().equals(dto.getCodigoInterno()) 
            && productoRepository.existsByCodigoInterno(dto.getCodigoInterno())) {
            throw new IllegalArgumentException("Ya existe otro producto con el código interno: " + dto.getCodigoInterno());
        }
        
        // Actualizar campos
        producto.setCodigoInterno(dto.getCodigoInterno());
        producto.setCodigoBarras(dto.getCodigoBarras());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setEmpresaCabysId(dto.getEmpresaCabysId());
        producto.setFamiliaId(dto.getFamiliaId());
        producto.setTipo(dto.getTipo());
        producto.setTipoInventario(dto.getTipoInventario());
        producto.setZonaPreparacion(dto.getZonaPreparacion());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setPrecioBase(dto.getPrecioBase());
        producto.setPrecioCompra(dto.getPrecioCompra());
        producto.setUnidadMedida(dto.getUnidadMedida());
        producto.setMoneda(dto.getMoneda());
        producto.setUnidadMedidaCompra(dto.getUnidadMedidaCompra());
        producto.setUnidadMedidaUso(dto.getUnidadMedidaUso());
        producto.setFactorConversion(dto.getFactorConversion());
        producto.setFactorConversionReceta(dto.getFactorConversionReceta());
        producto.setEsServicio(dto.getEsServicio());
        producto.setIncluyeIva(dto.getIncluyeIva());
        producto.setRequiereInventario(dto.getRequiereInventario());
        producto.setRequiereReceta(dto.getRequiereReceta());
        producto.setRequierePersonalizacion(dto.getRequierePersonalizacion());
        
        Producto updated = productoRepository.save(producto);
        log.info("Producto actualizado exitosamente");
        
        // Actualizar categorías si vienen en el DTO
        if (dto.getCategoriasIds() != null) {
            producto.getCategorias().clear();
            asignarCategorias(id, dto.getCategoriasIds());
        }
        
        // Actualizar impuestos si vienen en el DTO
        if (dto.getImpuestos() != null) {
            productoImpuestoRepository.deleteByProductoId(id);
            asignarImpuestos(id, dto.getImpuestos());
        }
        
        return ProductoDTO.fromEntity(productoRepository.findById(id).orElseThrow());
    }

    /**
     * Activar/Desactivar un producto
     */
    @Transactional
    public ProductoDTO cambiarEstadoProducto(Long id, Boolean activo) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Cambiando estado del producto {} a {} para tenant: {}", id, activo, tenant);
        
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        
        producto.setActivo(activo);
        Producto updated = productoRepository.save(producto);
        
        log.info("Estado del producto actualizado exitosamente");
        
        return ProductoDTO.fromEntity(updated);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Asignar categorías a un producto
     */
    private void asignarCategorias(Long productoId, List<Long> categoriasIds) {
        Producto producto = productoRepository.findById(productoId).orElseThrow();
        
        Set<CategoriaProducto> categorias = categoriasIds.stream()
            .map(catId -> categoriaProductoRepository.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + catId)))
            .collect(Collectors.toSet());
        
        producto.setCategorias(categorias);
        productoRepository.save(producto);
        
        log.debug("Asignadas {} categorías al producto {}", categorias.size(), productoId);
    }

    /**
     * Asignar impuestos a un producto
     */
    private void asignarImpuestos(Long productoId, List<CreateProductoImpuestoDTO> impuestosDto) {
        List<ProductoImpuesto> impuestos = impuestosDto.stream()
            .map(dto -> ProductoImpuesto.builder()
                .productoId(productoId)
                .tipoImpuesto(dto.getTipoImpuesto())
                .codigoTarifaIva(dto.getCodigoTarifaIva())
                .porcentaje(dto.getPorcentaje())
                .activo(true)
                .build())
            .collect(Collectors.toList());
        
        productoImpuestoRepository.saveAll(impuestos);
        
        log.debug("Asignados {} impuestos al producto {}", impuestos.size(), productoId);
    }

    /**
     * Validar si un código está disponible
     */
    @Transactional(readOnly = true)
    public boolean validarCodigoDisponible(String codigo, Long productoId) {
        if (productoId == null) {
            // Nuevo producto
            return !productoRepository.existsByCodigoInterno(codigo);
        } else {
            // Producto existente: verificar que no lo use otro
            Producto producto = productoRepository.findById(productoId).orElse(null);
            if (producto != null && producto.getCodigoInterno().equals(codigo)) {
                return true; // Es su propio código
            }
            return !productoRepository.existsByCodigoInterno(codigo);
        }
    }
}