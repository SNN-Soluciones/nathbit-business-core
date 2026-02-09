package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.CodigoCAByS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para CodigoCabys (schema PUBLIC).
 * Esta tabla NO usa multi-tenancy.
 */
@Repository
public interface CodigoCabysRepository extends JpaRepository<CodigoCAByS, Long> {

    /**
     * Buscar códigos CAByS activos con filtros opcionales
     */
    @Query("""
        SELECT c FROM CodigoCAByS c
        WHERE c.activo = true
        AND (:descripcion IS NULL OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%')))
        AND (:codigo IS NULL OR c.codigo LIKE CONCAT(:codigo, '%'))
        AND (:impuesto IS NULL OR c.impuestoSugerido = :impuesto)
        ORDER BY c.codigo ASC
        """)
    List<CodigoCAByS> buscarConFiltros(
        @Param("descripcion") String descripcion,
        @Param("codigo") String codigo,
        @Param("impuesto") String impuesto
    );
}