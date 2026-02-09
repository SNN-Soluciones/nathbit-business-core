package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

//import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para búsqueda avanzada de productos.
 * Todos los filtros son opcionales y combinables.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Schema(description = "Criterios de búsqueda de productos")
public class ProductoSearchDTO {

//    @Schema(description = "Búsqueda por código interno o código de barras")
    private String codigo;

//    @Schema(description = "Búsqueda por nombre (parcial, case-insensitive)")
    private String nombre;

//    @Schema(description = "Precio de venta (busca considerando impuestos aplicados)")
    private BigDecimal precio;

//    @Schema(description = "Tipo de producto (VENTA, MATERIA_PRIMA, MIXTO, COMBO, COMPUESTO)")
    private String tipo;

//    @Schema(description = "Zona de preparación (NINGUNA, COCINA, BAR, etc)")
    private String zona;

//    @Schema(description = "Código de tarifa de impuesto (ej: TARIFA_GENERAL_13, TARIFA_EXENTA)")
    private String codigoTarifaImpuesto;

//    @Schema(description = "Requiere receta para producción")
    private Boolean requiereReceta;

//    @Schema(description = "Requiere control de inventario")
    private Boolean requiereInventario;

//    @Schema(description = "ID de categoría")
    private Long categoriaId;

//    @Schema(description = "Solo productos activos (default: true)")
    @Builder.Default
    private Boolean activo = true;

//    @Schema(description = "Número de página (0-based)", example = "0")
    @Builder.Default
    private Integer page = 0;

//    @Schema(description = "Tamaño de página", example = "15")
    @Builder.Default
    private Integer size = 15;

//    @Schema(description = "Campo para ordenar", example = "nombre")
    @Builder.Default
    private String sortBy = "nombre";

//    @Schema(description = "Dirección de ordenamiento (asc/desc)", example = "asc")
    @Builder.Default
    private String sortDir = "asc";
}