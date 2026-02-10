package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.model.enums.TipoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // ==================== BÚSQUEDA POR CÓDIGO INTERNO ====================

    boolean existsByCodigoInterno(String codigoInterno);

    boolean existsByCodigoInternoAndIdNot(String codigoInterno, Long id);

    // ==================== BÚSQUEDA POR CÓDIGO DE BARRAS ====================

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

    // ==================== LISTAR ACTIVOS ====================

    Page<Producto> findByActivoTrue(Pageable pageable);

    List<Producto> findByActivoTrueOrderByNombreAsc();

    // ==================== BÚSQUEDA RÁPIDA ====================

    /**
     * Busca productos por código interno o nombre (case insensitive)
     * Solo productos activos
     */
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
        "(LOWER(p.codigoInterno) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
        "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Producto> buscarPorCodigoONombre(@Param("termino") String termino);

    // Versión alternativa con método generado (nombre largo pero funciona)
    List<Producto> findByCodigoInternoContainingIgnoreCaseOrNombreContainingIgnoreCaseAndActivoTrue(
        String codigoInterno, String nombre);

    // ==================== BÚSQUEDA POR CATEGORÍA ====================

    @Query("SELECT p FROM Producto p JOIN p.categorias c WHERE c.id = :categoriaId AND p.activo = true")
    Page<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId, Pageable pageable);

    // ==================== BÚSQUEDA POR TIPO ====================

    Page<Producto> findByTipoAndActivoTrue(TipoProducto tipo, Pageable pageable);

    // ==================== PARA INVENTARIO ====================

    @Query("SELECT p FROM Producto p WHERE p.requiereInventario = true AND p.activo = true")
    Page<Producto> findProductosParaInventario(Pageable pageable);
}