package com.snnsoluciones.nathbitbusinesscore.service.handler;

import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import com.snnsoluciones.nathbitbusinesscore.model.entity.CategoriaProducto;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.repository.CategoriaProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handler para gestionar las categorías de un producto
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoCategoriaHandler {

    private final CategoriaProductoRepository categoriaRepository;

    /**
     * Asigna categorías a un producto (creación)
     */
    public void asignarCategorias(Producto producto, List<Long> categoriasIds) {
        if (categoriasIds == null || categoriasIds.isEmpty()) {
            log.debug("No hay categorías para asignar al producto {}", producto.getCodigoInterno());
            return;
        }

        log.debug("📦 Asignando {} categorías al producto {}", categoriasIds.size(), producto.getCodigoInterno());

        Set<CategoriaProducto> categorias = obtenerCategorias(categoriasIds);
        producto.setCategorias(categorias);

        log.debug("✅ Categorías asignadas correctamente");
    }

    /**
     * Actualiza las categorías de un producto
     */
    public void actualizarCategorias(Producto producto, List<Long> categoriasIds) {
        log.debug("🔄 Actualizando categorías del producto {}", producto.getId());

        // Limpiar categorías actuales
        producto.getCategorias().clear();

        // Asignar nuevas categorías
        if (categoriasIds != null && !categoriasIds.isEmpty()) {
            Set<CategoriaProducto> nuevasCategorias = obtenerCategorias(categoriasIds);
            producto.setCategorias(nuevasCategorias);
            log.debug("✅ {} categorías actualizadas", categoriasIds.size());
        } else {
            log.debug("✅ Categorías eliminadas (sin categorías asignadas)");
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Obtiene las entidades de categorías desde los IDs
     */
    private Set<CategoriaProducto> obtenerCategorias(List<Long> categoriasIds) {
        Set<CategoriaProducto> categorias = new HashSet<>();

        for (Long categoriaId : categoriasIds) {
            CategoriaProducto categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new BusinessException(
                    "Categoría no encontrada con ID: " + categoriaId
                ));

            if (!categoria.getActivo()) {
                throw new BusinessException(
                    "La categoría '" + categoria.getNombre() + "' está inactiva"
                );
            }

            categorias.add(categoria);
        }

        return categorias;
    }
}