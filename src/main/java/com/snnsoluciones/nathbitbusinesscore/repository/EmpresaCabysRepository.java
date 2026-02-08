package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.EmpresaCabys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para EmpresaCabys.
 * Accede a tenant_X.empresa_cabys (schema dinámico vía SET search_path)
 */
@Repository
public interface EmpresaCabysRepository extends JpaRepository<EmpresaCabys, Long> {

    /**
     * Buscar todos los CAByS activos
     */
    List<EmpresaCabys> findByActivoTrue();

    /**
     * Buscar por codigo_cabys_id
     */
    Optional<EmpresaCabys> findByCodigoCabysId(Long codigoCabysId);

    /**
     * Buscar por código CAByS
     */
    Optional<EmpresaCabys> findByCodigo(String codigo);

    /**
     * Verificar si existe un código CAByS asignado
     */
    boolean existsByCodigoCabysId(Long codigoCabysId);

    /**
     * Contar CAByS activos
     */
    long countByActivoTrue();
}