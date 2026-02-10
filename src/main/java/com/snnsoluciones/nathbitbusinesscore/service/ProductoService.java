package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.*;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.model.enums.TipoInventario;
import com.snnsoluciones.nathbitbusinesscore.model.enums.TipoProducto;
import com.snnsoluciones.nathbitbusinesscore.model.enums.UnidadMedida;
import com.snnsoluciones.nathbitbusinesscore.model.enums.ZonaPreparacion;
import com.snnsoluciones.nathbitbusinesscore.model.enums.mh.Moneda;
import com.snnsoluciones.nathbitbusinesscore.repository.ProductoRepository;
import com.snnsoluciones.nathbitbusinesscore.service.handler.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio completo de productos con handlers integrados
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    // Repositories
    private final ProductoRepository productoRepository;

    // Handlers
    private final ProductoValidador productoValidador;
    private final ProductoCategoriaHandler categoriaHandler;
    private final ProductoImpuestoHandler impuestoHandler;
    private final ProductoImagenHandler imagenHandler;
    private final ProductoTributacionHandler tributacionHandler;

    // ==================== CREAR ====================

    /**
     * Crea un nuevo producto
     */
    @Transactional
    public ProductoDTO crearProducto(CreateProductoDTO dto) {
        log.info("📦 Creando producto: {}", dto.getCodigoInterno());

        // 1️⃣ Validar datos
        productoValidador.validarCreacion(dto);

        // 2️⃣ Configurar impuestos según régimen tributario
        tributacionHandler.configurarImpuestosSegunRegimen(dto);

        // 3️⃣ Crear entidad
        Producto producto = Producto.builder()
            .codigoInterno(dto.getCodigoInterno())
            .codigoBarras(dto.getCodigoBarras())
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .empresaCabysId(dto.getEmpresaCabysId())
            .familiaId(dto.getFamiliaId())
            .tipo(TipoProducto.valueOf(dto.getTipo()))
            .tipoInventario(TipoInventario.valueOf(dto.getTipoInventario()))
            .zonaPreparacion(ZonaPreparacion.valueOf(dto.getZonaPreparacion()))
            .precioVenta(dto.getPrecioVenta())
            .precioBase(dto.getPrecioBase())
            .precioCompra(dto.getPrecioCompra())
            .unidadMedida(UnidadMedida.valueOf(dto.getUnidadMedida()))
            .moneda(Moneda.valueOf(dto.getMoneda()))
            .unidadMedidaCompra(dto.getUnidadMedidaCompra() != null 
                ? UnidadMedida.valueOf(dto.getUnidadMedidaCompra()) : null)
            .unidadMedidaUso(dto.getUnidadMedidaUso() != null 
                ? UnidadMedida.valueOf(dto.getUnidadMedidaUso()) : null)
            .factorConversion(dto.getFactorConversion())
            .factorConversionReceta(dto.getFactorConversionReceta())
            .esServicio(dto.getEsServicio())
            .incluyeIva(dto.getIncluyeIva())
            .requiereInventario(dto.getRequiereInventario())
            .requiereReceta(dto.getRequiereReceta())
            .requierePersonalizacion(dto.getRequierePersonalizacion())
            .activo(true)
            .build();

        // 4️⃣ Asignar categorías
        categoriaHandler.asignarCategorias(producto, dto.getCategoriasIds());

        // 5️⃣ Asignar impuestos
        impuestoHandler.asignarImpuestos(producto, dto.getImpuestos());

        // 6️⃣ Guardar producto
        Producto guardado = productoRepository.save(producto);

        log.info("✅ Producto creado con ID: {}", guardado.getId());

        return convertirADto(guardado);
    }

    /**
     * Crea un producto con imagen
     */
    @Transactional
    public ProductoDTO crearProductoConImagen(CreateProductoDTO dto, MultipartFile imagen) {
        log.info("📦📸 Creando producto con imagen: {}", dto.getCodigoInterno());

        // Crear producto
        ProductoDTO productoDto = crearProducto(dto);

        // Subir imagen
        Producto producto = productoRepository.findById(productoDto.getId())
            .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        imagenHandler.subirImagen(producto, imagen);

        return convertirADto(producto);
    }

    // ==================== ACTUALIZAR ====================

    /**
     * Actualiza un producto existente
     */
    @Transactional
    public ProductoDTO actualizarProducto(Long id, UpdateProductoDTO dto) {
        log.info("🔄 Actualizando producto ID: {}", id);

        // 1️⃣ Validar datos
        productoValidador.validarActualizacion(id, dto);

        // 2️⃣ Obtener producto existente
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Producto no encontrado con ID: " + id));

        // 3️⃣ Configurar impuestos según régimen (si cambió incluyeIva o impuestos)
        if (dto.getImpuestos() != null) {
            CreateProductoDTO dtoTemp = convertirUpdateACreate(dto, producto);
            tributacionHandler.configurarImpuestosSegunRegimen(dtoTemp);
            dto.setImpuestos(dtoTemp.getImpuestos());
            dto.setIncluyeIva(dtoTemp.getIncluyeIva());
        }

        // 4️⃣ Actualizar campos básicos
        actualizarCamposBasicos(producto, dto);

        // 5️⃣ Actualizar categorías (si se especificaron)
        if (dto.getCategoriasIds() != null) {
            categoriaHandler.actualizarCategorias(producto, dto.getCategoriasIds());
        }

        // 6️⃣ Actualizar impuestos (si se especificaron)
        if (dto.getImpuestos() != null) {
            impuestoHandler.actualizarImpuestos(producto, dto.getImpuestos());
        }

        // 7️⃣ Guardar cambios
        Producto actualizado = productoRepository.save(producto);

        log.info("✅ Producto actualizado: {}", id);

        return convertirADto(actualizado);
    }

    /**
     * Actualiza solo la imagen del producto
     */
    @Transactional
    public ProductoDTO actualizarImagen(Long id, MultipartFile imagen) {
        log.info("📸 Actualizando imagen del producto ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Producto no encontrado con ID: " + id));

        imagenHandler.actualizarImagen(producto, imagen);

        return convertirADto(producto);
    }

    /**
     * Elimina la imagen del producto
     */
    @Transactional
    public ProductoDTO eliminarImagen(Long id) {
        log.info("🗑️ Eliminando imagen del producto ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Producto no encontrado con ID: " + id));

        imagenHandler.eliminarImagen(producto);

        return convertirADto(producto);
    }

    /**
     * Cambia el estado de un producto (activo/inactivo)
     */
    @Transactional
    public ProductoDTO cambiarEstado(Long id, boolean activo) {
        log.info("🔄 Cambiando estado del producto ID: {} a {}", id, activo ? "ACTIVO" : "INACTIVO");

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Producto no encontrado con ID: " + id));

        producto.setActivo(activo);
        producto.setUpdatedAt(LocalDateTime.now());

        Producto guardado = productoRepository.save(producto);

        log.info("✅ Estado actualizado");

        return convertirADto(guardado);
    }

    // ==================== ELIMINAR ====================

    /**
     * Elimina un producto (soft delete - marca como inactivo)
     */
    @Transactional
    public void eliminarProducto(Long id) {
        log.info("🗑️ Eliminando producto ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Producto no encontrado con ID: " + id));

        // Soft delete
        producto.setActivo(false);
        productoRepository.save(producto);

        log.info("✅ Producto marcado como inactivo");
    }

    // ==================== CONSULTAS ====================

    /**
     * Obtiene un producto por ID
     */
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        log.debug("🔍 Buscando producto ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Producto no encontrado con ID: " + id));

        return convertirADto(producto);
    }

    /**
     * Lista todos los productos activos (paginado)
     */
    @Transactional(readOnly = true)
    public ProductoPage listarActivos(int page, int size, String sortBy, String sortDir) {
        log.debug("📋 Listando productos activos - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Producto> pageResult = productoRepository.findByActivoTrue(pageable);

        return convertirAProductoPage(pageResult);
    }

    /**
     * Lista todos los productos (paginado)
     */
    @Transactional(readOnly = true)
    public ProductoPage listarTodos(int page, int size) {
        log.debug("📋 Listando todos los productos - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Producto> pageResult = productoRepository.findAll(pageable);

        return convertirAProductoPage(pageResult);
    }

    /**
     * Busca productos por término (código o nombre)
     */
    @Transactional(readOnly = true)
    public List<ProductoListDTO> buscarRapido(String termino) {
        log.debug("🔍 Búsqueda rápida: {}", termino);

        if (termino == null || termino.trim().length() < 3) {
            throw new BusinessException("El término de búsqueda debe tener al menos 3 caracteres");
        }

        List<Producto> productos = productoRepository.findByCodigoInternoContainingIgnoreCaseOrNombreContainingIgnoreCaseAndActivoTrue(
            termino, termino);

        return productos.stream()
            .map(this::convertirAListDto)
            .collect(Collectors.toList());
    }

    /**
     * Valida disponibilidad de código
     */
    @Transactional(readOnly = true)
    public boolean validarCodigo(String codigo, Long productoId) {
        log.debug("🔍 Validando código: {}", codigo);

        boolean existe = productoId != null
            ? productoRepository.existsByCodigoInternoAndIdNot(codigo, productoId)
            : productoRepository.existsByCodigoInterno(codigo);

        return !existe; // Retorna true si está disponible
    }

    // ==================== CONVERSORES ====================

    private ProductoDTO convertirADto(Producto producto) {
        return ProductoDTO.builder()
            .id(producto.getId())
            .codigoInterno(producto.getCodigoInterno())
            .codigoBarras(producto.getCodigoBarras())
            .nombre(producto.getNombre())
            .descripcion(producto.getDescripcion())
            .empresaCabysId(producto.getEmpresaCabysId())
            .familiaId(producto.getFamiliaId())
            .tipo(producto.getTipo().name())
            .tipoInventario(producto.getTipoInventario().name())
            .zonaPreparacion(producto.getZonaPreparacion().name())
            .precioVenta(producto.getPrecioVenta())
            .precioBase(producto.getPrecioBase())
            .precioCompra(producto.getPrecioCompra())
            .ultimoPrecioCompra(producto.getUltimoPrecioCompra())
            .unidadMedida(producto.getUnidadMedida().name())
            .moneda(producto.getMoneda().name())
            .unidadMedidaCompra(producto.getUnidadMedidaCompra() != null 
                ? producto.getUnidadMedidaCompra().name() : null)
            .unidadMedidaUso(producto.getUnidadMedidaUso() != null 
                ? producto.getUnidadMedidaUso().name() : null)
            .factorConversion(producto.getFactorConversion())
            .factorConversionReceta(producto.getFactorConversionReceta())
            .activo(producto.getActivo())
            .esServicio(producto.getEsServicio())
            .incluyeIva(producto.getIncluyeIva())
            .requiereInventario(producto.getRequiereInventario())
            .requiereReceta(producto.getRequiereReceta())
            .requierePersonalizacion(producto.getRequierePersonalizacion())
            .imagenUrl(producto.getImagenUrl())
            .imagenKey(producto.getImagenKey())
            .thumbnailUrl(producto.getThumbnailUrl())
            .thumbnailKey(producto.getThumbnailKey())
            .fechaUltimaCompra(producto.getFechaUltimaCompra())
            .createdAt(producto.getCreatedAt())
            .updatedAt(producto.getUpdatedAt())
            .build();
    }

    private ProductoListDTO convertirAListDto(Producto producto) {
        return ProductoListDTO.builder()
            .id(producto.getId())
            .codigoInterno(producto.getCodigoInterno())
            .nombre(producto.getNombre())
            .tipo(producto.getTipo().name())
            .zonaPreparacion(producto.getZonaPreparacion().name())
            .precioVenta(producto.getPrecioVenta())
            .thumbnailUrl(producto.getThumbnailUrl())
            .activo(producto.getActivo())
            .requiereInventario(producto.getRequiereInventario())
            .requiereReceta(producto.getRequiereReceta())
            .build();
    }

    private ProductoPage convertirAProductoPage(Page<Producto> page) {
        List<ProductoListDTO> productos = page.getContent().stream()
            .map(this::convertirAListDto)
            .collect(Collectors.toList());

        return ProductoPage.builder()
            .productos(productos)
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .build();
    }

    private void actualizarCamposBasicos(Producto producto, UpdateProductoDTO dto) {
        if (dto.getCodigoInterno() != null) producto.setCodigoInterno(dto.getCodigoInterno());
        if (dto.getCodigoBarras() != null) producto.setCodigoBarras(dto.getCodigoBarras());
        if (dto.getNombre() != null) producto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) producto.setDescripcion(dto.getDescripcion());
        if (dto.getEmpresaCabysId() != null) producto.setEmpresaCabysId(dto.getEmpresaCabysId());
        if (dto.getFamiliaId() != null) producto.setFamiliaId(dto.getFamiliaId());
        if (dto.getTipo() != null) producto.setTipo(TipoProducto.valueOf(dto.getTipo()));
        if (dto.getTipoInventario() != null) producto.setTipoInventario(TipoInventario.valueOf(dto.getTipoInventario()));
        if (dto.getZonaPreparacion() != null) producto.setZonaPreparacion(ZonaPreparacion.valueOf(dto.getZonaPreparacion()));
        if (dto.getPrecioVenta() != null) producto.setPrecioVenta(dto.getPrecioVenta());
        if (dto.getPrecioBase() != null) producto.setPrecioBase(dto.getPrecioBase());
        if (dto.getPrecioCompra() != null) producto.setPrecioCompra(dto.getPrecioCompra());
        if (dto.getUnidadMedida() != null) producto.setUnidadMedida(UnidadMedida.valueOf(dto.getUnidadMedida()));
        if (dto.getMoneda() != null) producto.setMoneda(Moneda.valueOf(dto.getMoneda()));
        if (dto.getUnidadMedidaCompra() != null) producto.setUnidadMedidaCompra(UnidadMedida.valueOf(dto.getUnidadMedidaCompra()));
        if (dto.getUnidadMedidaUso() != null) producto.setUnidadMedidaUso(UnidadMedida.valueOf(dto.getUnidadMedidaUso()));
        if (dto.getFactorConversion() != null) producto.setFactorConversion(dto.getFactorConversion());
        if (dto.getFactorConversionReceta() != null) producto.setFactorConversionReceta(dto.getFactorConversionReceta());
        if (dto.getEsServicio() != null) producto.setEsServicio(dto.getEsServicio());
        if (dto.getIncluyeIva() != null) producto.setIncluyeIva(dto.getIncluyeIva());
        if (dto.getRequiereInventario() != null) producto.setRequiereInventario(dto.getRequiereInventario());
        if (dto.getRequiereReceta() != null) producto.setRequiereReceta(dto.getRequiereReceta());
        if (dto.getRequierePersonalizacion() != null) producto.setRequierePersonalizacion(dto.getRequierePersonalizacion());

        producto.setUpdatedAt(LocalDateTime.now());
    }

    private CreateProductoDTO convertirUpdateACreate(UpdateProductoDTO dto, Producto producto) {
        return CreateProductoDTO.builder()
            .codigoInterno(dto.getCodigoInterno() != null ? dto.getCodigoInterno() : producto.getCodigoInterno())
            .incluyeIva(dto.getIncluyeIva() != null ? dto.getIncluyeIva() : producto.getIncluyeIva())
            .impuestos(dto.getImpuestos())
            .build();
    }
}