package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.context.TenantContext;
import com.snnsoluciones.nathbitbusinesscore.model.dto.CreateCategoriaProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.dto.CategoriaProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.entity.CategoriaProducto;
import com.snnsoluciones.nathbitbusinesscore.repository.CategoriaProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Categorías de Productos.
 * Requiere tenant configurado en TenantContext.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaProductoService {

    private final CategoriaProductoRepository categoriaProductoRepository;

    /**
     * Obtener todas las categorías activas ordenadas
     * 
     * @return Lista de categorías activas
     */
    @Transactional(readOnly = true)
    public List<CategoriaProductoDTO> obtenerCategoriasActivas() {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo categorías activas para tenant: {}", tenant);
        
        List<CategoriaProducto> categorias = categoriaProductoRepository.findByActivoTrueOrderByOrdenAsc();
        
        log.debug("Se encontraron {} categorías activas", categorias.size());
        
        return categorias.stream()
                .map(CategoriaProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las categorías (activas e inactivas)
     * 
     * @return Lista de todas las categorías
     */
    @Transactional(readOnly = true)
    public List<CategoriaProductoDTO> obtenerTodasCategorias() {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo todas las categorías para tenant: {}", tenant);
        
        List<CategoriaProducto> categorias = categoriaProductoRepository.findAllByOrderByOrdenAsc();
        
        log.debug("Se encontraron {} categorías en total", categorias.size());
        
        return categorias.stream()
                .map(CategoriaProductoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una categoría por ID
     * 
     * @param id ID de la categoría
     * @return DTO de la categoría o null si no existe
     */
    @Transactional(readOnly = true)
    public CategoriaProductoDTO obtenerCategoriaPorId(Long id) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo categoría con ID {} para tenant: {}", id, tenant);
        
        return categoriaProductoRepository.findById(id)
                .map(CategoriaProductoDTO::fromEntity)
                .orElse(null);
    }

    /**
     * Crear una nueva categoría
     * 
     * @param dto Datos de la categoría
     * @return Categoría creada
     */
    @Transactional
    public CategoriaProductoDTO crearCategoria(CreateCategoriaProductoDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Creando nueva categoría para tenant: {}", tenant);
        
        // Verificar si ya existe una categoría con ese nombre
        if (categoriaProductoRepository.existsByNombre(dto.getNombre())) {
            log.warn("Ya existe una categoría con el nombre: {}", dto.getNombre());
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }
        
        // Si no se especifica orden, agregar al final
        Integer orden = dto.getOrden();
        if (orden == null) {
            orden = categoriaProductoRepository.findMaxOrden() + 1;
            log.debug("Orden no especificado, asignando orden: {}", orden);
        }
        
        // Crear la entidad
        CategoriaProducto categoria = CategoriaProducto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .color(dto.getColor())
                .icono(dto.getIcono())
                .orden(orden)
                .activo(true)
                .build();
        
        CategoriaProducto saved = categoriaProductoRepository.save(categoria);
        
        log.info("Categoría creada exitosamente con ID: {}", saved.getId());
        
        return CategoriaProductoDTO.fromEntity(saved);
    }

    /**
     * Actualizar una categoría existente
     * 
     * @param id ID de la categoría
     * @param dto Datos actualizados
     * @return Categoría actualizada
     */
    @Transactional
    public CategoriaProductoDTO actualizarCategoria(Long id, CreateCategoriaProductoDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Actualizando categoría con ID {} para tenant: {}", id, tenant);
        
        CategoriaProducto categoria = categoriaProductoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
        
        // Verificar si el nombre ya existe en otra categoría
        if (categoriaProductoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            log.warn("Ya existe otra categoría con el nombre: {}", dto.getNombre());
            throw new IllegalArgumentException("Ya existe otra categoría con el nombre: " + dto.getNombre());
        }
        
        // Actualizar campos
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setColor(dto.getColor());
        categoria.setIcono(dto.getIcono());
        categoria.setOrden(dto.getOrden());
        
        CategoriaProducto updated = categoriaProductoRepository.save(categoria);
        
        log.info("Categoría actualizada exitosamente");
        
        return CategoriaProductoDTO.fromEntity(updated);
    }

    /**
     * Activar/Desactivar una categoría
     * 
     * @param id ID de la categoría
     * @param activo true para activar, false para desactivar
     * @return Categoría actualizada
     */
    @Transactional
    public CategoriaProductoDTO cambiarEstadoCategoria(Long id, boolean activo) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Cambiando estado de categoría con ID {} a {} para tenant: {}", id, activo, tenant);
        
        CategoriaProducto categoria = categoriaProductoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
        
        categoria.setActivo(activo);
        
        CategoriaProducto updated = categoriaProductoRepository.save(categoria);
        
        log.info("Estado de categoría cambiado exitosamente a: {}", activo);
        
        return CategoriaProductoDTO.fromEntity(updated);
    }

    /**
     * Eliminar una categoría (soft delete - desactivar)
     * 
     * @param id ID de la categoría
     */
    @Transactional
    public void eliminarCategoria(Long id) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Eliminando (desactivando) categoría con ID {} para tenant: {}", id, tenant);
        
        cambiarEstadoCategoria(id, false);
        
        log.info("Categoría desactivada exitosamente");
    }

    /**
     * Eliminar permanentemente una categoría
     * 
     * @param id ID de la categoría
     */
    @Transactional
    public void eliminarCategoriaPermanente(Long id) {
        String tenant = TenantContext.getCurrentTenant();
        log.warn("Eliminando PERMANENTEMENTE categoría con ID {} para tenant: {}", id, tenant);
        
        if (!categoriaProductoRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoría no encontrada con ID: " + id);
        }
        
        categoriaProductoRepository.deleteById(id);
        
        log.info("Categoría eliminada permanentemente");
    }
}