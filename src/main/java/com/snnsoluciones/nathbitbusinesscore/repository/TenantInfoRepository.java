package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.TenantInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantInfoRepository extends JpaRepository<TenantInfo, Long> {
    
    Optional<TenantInfo> findByCodigoTenant(String codigoTenant);
    
    Optional<TenantInfo> findFirstByOrderByIdAsc();
}