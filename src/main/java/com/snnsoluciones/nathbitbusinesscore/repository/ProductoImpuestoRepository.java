package com.snnsoluciones.nathbitbusinesscore.repository;

import com.snnsoluciones.nathbitbusinesscore.model.entity.ProductoImpuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoImpuestoRepository extends JpaRepository<ProductoImpuesto, Long> {
    
    /**
     * Busca todos los impuestos de un producto
     */
    List<ProductoImpuesto> findByProductoId(Long productoId);
    
    /**
     * Busca impuestos activos de un producto
     */
    List<ProductoImpuesto> findByProductoIdAndActivoTrue(Long productoId);
    
    /**
     * Elimina todos los impuestos de un producto
     */
    @Modifying
    @Query("DELETE FROM ProductoImpuesto pi WHERE pi.producto.id = :productoId")
    void deleteByProductoId(@Param("productoId") Long productoId);
}