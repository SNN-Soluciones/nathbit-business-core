package com.snnsoluciones.nathbitbusinesscore.model.entity;

import com.snnsoluciones.nathbitbusinesscore.model.enums.mh.CodigoTarifaIVA;
import com.snnsoluciones.nathbitbusinesscore.model.enums.mh.TipoImpuesto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto_impuestos",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_impuesto", 
            columnNames = {"producto_id", "tipo_impuesto"})
    },
    indexes = {
        @Index(name = "idx_producto_impuestos_producto", columnList = "producto_id")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImpuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_impuesto", nullable = false, length = 50)
    private TipoImpuesto tipoImpuesto;

    @Column(name = "codigo_tarifa_iva", nullable = false, length = 50)
    private CodigoTarifaIVA codigoTarifaIva;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}