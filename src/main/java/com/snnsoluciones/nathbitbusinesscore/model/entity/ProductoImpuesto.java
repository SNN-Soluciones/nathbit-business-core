package com.snnsoluciones.nathbitbusinesscore.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Impuestos de productos del tenant.
 * Tabla: tenant_X.producto_impuestos
 * 
 * Requiere:
 * - Device Token: Para acceso al tenant
 * - Bearer Token: Para auditar quién crea/modifica
 */
@Entity
@Table(name = "producto_impuestos", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_impuesto", 
            columnNames = {"producto_id", "tipo_impuesto"})
    },
    indexes = {
        @Index(name = "idx_producto_impuestos_producto", columnList = "producto_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImpuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "tipo_impuesto", nullable = false, length = 50)
    private String tipoImpuesto; // IVA, etc

    @Column(name = "codigo_tarifa_iva", length = 50)
    private String codigoTarifaIva; // TARIFA_GENERAL_13, TARIFA_EXENTA, etc

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // ==================== LIFECYCLE ====================
    
    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
    }
}