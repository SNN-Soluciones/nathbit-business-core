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
     * 
     * NOTA: Usamos nativeQuery con CAST explícito para evitar problemas
     * de tipo cuando los parámetros son null
     */
    @Query(value = """
        SELECT * FROM public.codigos_cabys c
        WHERE c.activo = true
        AND (CAST(:descripcion AS TEXT) IS NULL OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%')))
        AND (CAST(:codigo AS TEXT) IS NULL OR c.codigo LIKE CONCAT(:codigo, '%'))
        AND (CAST(:impuesto AS TEXT) IS NULL OR c.impuesto_sugerido = :impuesto)
        ORDER BY c.codigo ASC
        """, nativeQuery = true)
    List<CodigoCAByS> buscarConFiltros(
        @Param("descripcion") String descripcion,
        @Param("codigo") String codigo,
        @Param("impuesto") String impuesto
    );
}