package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para CategoriaProducto.
 * Accede a tenant_X.categorias_producto (schema dinámico vía SET search_path)
 */
@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {

    /**
     * Buscar todas las categorías activas ordenadas por 'orden'
     */
    List<CategoriaProducto> findByActivoTrueOrderByOrdenAsc();

    /**
     * Buscar todas las categorías (activas e inactivas) ordenadas
     */
    List<CategoriaProducto> findAllByOrderByOrdenAsc();

    /**
     * Contar categorías activas
     */
    long countByActivoTrue();

    /**
     * Buscar por nombre (exacto)
     */
    Optional<CategoriaProducto> findByNombre(String nombre);

    /**
     * Verificar si existe una categoría con ese nombre
     */
    boolean existsByNombre(String nombre);

    /**
     * Verificar si existe una categoría con ese nombre, excluyendo un ID específico
     * (útil para validar al actualizar)
     */
    boolean existsByNombreAndIdNot(String nombre, Long id);

    /**
     * Obtener el máximo orden actual (para agregar al final)
     */
    @Query("SELECT COALESCE(MAX(c.orden), 0) FROM CategoriaProducto c")
    Integer findMaxOrden();
}