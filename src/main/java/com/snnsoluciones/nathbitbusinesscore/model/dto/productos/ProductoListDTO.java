package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO resumido para listados de productos.
 * Solo campos esenciales para optimizar performance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoListDTO {

    private Long id;
    private String codigoInterno;
    private String codigoBarras;
    private String nombre;
    private String tipo;
    private String zonaPreparacion;
    private BigDecimal precioVenta;
    private String unidadMedida;
    private Boolean activo;
    private Boolean requiereInventario;
    private Boolean requiereReceta;
    private String thumbnailUrl;

    /**
     * Convierte una entidad a DTO resumido
     */
    public static ProductoListDTO fromEntity(Producto entity) {
        if (entity == null) {
            return null;
        }
        
        return ProductoListDTO.builder()
                .id(entity.getId())
                .codigoInterno(entity.getCodigoInterno())
                .codigoBarras(entity.getCodigoBarras())
                .nombre(entity.getNombre())
                .tipo(entity.getTipo())
                .zonaPreparacion(entity.getZonaPreparacion())
                .precioVenta(entity.getPrecioVenta())
                .unidadMedida(entity.getUnidadMedida())
                .activo(entity.getActivo())
                .requiereInventario(entity.getRequiereInventario())
                .requiereReceta(entity.getRequiereReceta())
                .thumbnailUrl(entity.getThumbnailUrl())
                .build();
    }
}