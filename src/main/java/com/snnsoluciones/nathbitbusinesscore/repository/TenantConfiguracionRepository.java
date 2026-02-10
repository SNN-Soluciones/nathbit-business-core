package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.TenantConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantConfiguracionRepository extends JpaRepository<TenantConfiguracion, Long> {
    
    Optional<TenantConfiguracion> findByCodigoTenant(String codigoTenant);
    
    Optional<TenantConfiguracion> findFirstByOrderByIdAsc();
}