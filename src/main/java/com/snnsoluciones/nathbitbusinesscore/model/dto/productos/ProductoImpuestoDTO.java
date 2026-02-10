package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

import com.snnsoluciones.nathbitbusinesscore.model.entity.ProductoImpuesto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para impuestos de productos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImpuestoDTO {

    private Long id;
    private Long productoId;
    private String tipoImpuesto;
    private String codigoTarifaIva;
    private BigDecimal porcentaje;
    private Boolean activo;

    /**
     * Convierte una entidad a DTO
     */
    public static ProductoImpuestoDTO fromEntity(ProductoImpuesto entity) {
        if (entity == null) {
            return null;
        }
        
        return ProductoImpuestoDTO.builder()
                .id(entity.getId())
                .productoId(entity.getProducto().getId())
                .tipoImpuesto(entity.getTipoImpuesto().name())
                .codigoTarifaIva(entity.getCodigoTarifaIva().getCodigo())
                .porcentaje(entity.getPorcentaje())
                .activo(entity.getActivo())
                .build();
    }
}