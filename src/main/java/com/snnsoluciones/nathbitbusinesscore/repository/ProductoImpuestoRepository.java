package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.ProductoImpuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ProductoImpuesto.
 * Accede a tenant_X.producto_impuestos (schema dinámico vía SET search_path)
 */
@Repository
public interface ProductoImpuestoRepository extends JpaRepository<ProductoImpuesto, Long> {

    /**
     * Buscar impuestos de un producto
     */
    List<ProductoImpuesto> findByProductoId(Long productoId);

    /**
     * Buscar impuestos activos de un producto
     */
    List<ProductoImpuesto> findByProductoIdAndActivoTrue(Long productoId);

    /**
     * Buscar un impuesto específico de un producto
     */
    Optional<ProductoImpuesto> findByProductoIdAndTipoImpuesto(Long productoId, String tipoImpuesto);

    /**
     * Verificar si existe un impuesto para un producto
     */
    boolean existsByProductoIdAndTipoImpuesto(Long productoId, String tipoImpuesto);

    /**
     * Eliminar todos los impuestos de un producto
     */
    void deleteByProductoId(Long productoId);
}