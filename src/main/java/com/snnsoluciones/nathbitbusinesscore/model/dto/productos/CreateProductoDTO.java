package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para crear un nuevo producto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductoDTO {

    // Códigos
    @NotBlank(message = "El código interno es obligatorio")
    @Size(max = 20, message = "El código interno no puede exceder 20 caracteres")
    private String codigoInterno;

    @Size(max = 30, message = "El código de barras no puede exceder 30 caracteres")
    private String codigoBarras;

    // Info básica
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    private String descripcion;

    // Relaciones
    private Long empresaCabysId;
    private Long familiaId;

    // Tipo y control
    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotBlank(message = "El tipo de inventario es obligatorio")
    private String tipoInventario;

    @NotBlank(message = "La zona de preparación es obligatoria")
    private String zonaPreparacion;

    // Precios
    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioVenta;

    private BigDecimal precioBase;
    private BigDecimal precioCompra;

    // Unidades
    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    @NotBlank(message = "La moneda es obligatoria")
    private String moneda;

    private String unidadMedidaCompra;
    private String unidadMedidaUso;

    // Factores
    private BigDecimal factorConversion;
    
    @Builder.Default
    private BigDecimal factorConversionReceta = BigDecimal.ONE;

    // Flags
    @Builder.Default
    private Boolean esServicio = false;

    @Builder.Default
    private Boolean incluyeIva = true;

    @Builder.Default
    private Boolean requiereInventario = false;

    @Builder.Default
    private Boolean requiereReceta = false;

    @Builder.Default
    private Boolean requierePersonalizacion = false;

    // Categorías e Impuestos
    private List<Long> categoriasIds;
    private List<CreateProductoImpuestoDTO> impuestos;
}