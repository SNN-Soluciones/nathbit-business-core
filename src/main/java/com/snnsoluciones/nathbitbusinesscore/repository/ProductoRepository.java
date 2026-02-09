package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Productos.
 * Accede a tenant_X.productos (schema dinámico vía SET search_path)
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, 
                                            JpaSpecificationExecutor<Producto> {

    /**
     * Buscar por código interno
     */
    Optional<Producto> findByCodigoInterno(String codigoInterno);

    /**
     * Buscar por código de barras
     */
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    /**
     * Verificar si existe por código interno
     */
    boolean existsByCodigoInterno(String codigoInterno);

    /**
     * Verificar si existe por código de barras
     */
    boolean existsByCodigoBarras(String codigoBarras);

    /**
     * Buscar productos activos
     */
    Page<Producto> findByActivoTrue(Pageable pageable);

    /**
     * Buscar productos por tipo
     */
    Page<Producto> findByTipo(String tipo, Pageable pageable);

    /**
     * Buscar productos activos por tipo
     */
    Page<Producto> findByActivoTrueAndTipo(String tipo, Pageable pageable);

    /**
     * Buscar productos por zona de preparación
     */
    Page<Producto> findByZonaPreparacion(String zona, Pageable pageable);

    /**
     * Buscar productos por nombre (parcial, case-insensitive)
     */
    @Query("""
        SELECT p FROM Producto p 
        WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))
        AND p.activo = true
        ORDER BY p.nombre ASC
        """)
    List<Producto> buscarPorNombre(@Param("termino") String termino);

    /**
     * Buscar productos por código (interno o barras)
     */
    @Query("""
        SELECT p FROM Producto p 
        WHERE (LOWER(p.codigoInterno) LIKE LOWER(CONCAT('%', :termino, '%'))
            OR LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :termino, '%')))
        AND p.activo = true
        ORDER BY p.codigoInterno ASC
        """)
    List<Producto> buscarPorCodigo(@Param("termino") String termino);

    /**
     * Buscar productos que requieren inventario
     */
    @Query("""
        SELECT p FROM Producto p 
        WHERE p.requiereInventario = true
        AND p.tipoInventario = 'SIMPLE'
        AND p.tipo IN ('VENTA', 'MIXTO', 'MATERIA_PRIMA')
        AND p.activo = true
        AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))
            OR LOWER(p.codigoInterno) LIKE LOWER(CONCAT('%', :termino, '%')))
        """)
    Page<Producto> buscarParaInventario(@Param("termino") String termino, Pageable pageable);

    /**
     * Buscar productos por categoría
     */
    @Query("""
        SELECT DISTINCT p FROM Producto p 
        JOIN p.categorias c
        WHERE c.id = :categoriaId
        AND p.activo = true
        """)
    Page<Producto> buscarPorCategoria(@Param("categoriaId") Long categoriaId, Pageable pageable);

    // ❌ ELIMINADO: buscarPorImpuesto() - No tenemos relación con impuestos

    /**
     * Contar productos activos
     */
    long countByActivoTrue();

    /**
     * Contar productos por tipo
     */
    long countByTipo(String tipo);

    /**
     * Obtener código interno máximo para generar siguiente
     */
    @Query("""
        SELECT p.codigoInterno FROM Producto p 
        WHERE p.codigoInterno LIKE :prefix%
        ORDER BY p.codigoInterno DESC
        """)
    List<String> findMaxCodigoInternoByPrefix(@Param("prefix") String prefix, Pageable pageable);
}