package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para actualizar un producto existente
 * Todos los campos son opcionales - solo se actualizan los que se envían
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductoDTO {

    // Códigos
    @Size(max = 20, message = "El código interno no puede exceder 20 caracteres")
    private String codigoInterno;

    @Size(max = 30, message = "El código de barras no puede exceder 30 caracteres")
    private String codigoBarras;

    // Info básica
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    private String descripcion;

    // Relaciones
    private Long empresaCabysId;
    private Long familiaId;

    // Tipo y control
    private String tipo;
    private String tipoInventario;
    private String zonaPreparacion;

    // Precios
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioVenta;

    private BigDecimal precioBase;
    private BigDecimal precioCompra;

    // Unidades
    private String unidadMedida;
    private String moneda;
    private String unidadMedidaCompra;
    private String unidadMedidaUso;

    // Factores
    private BigDecimal factorConversion;
    private BigDecimal factorConversionReceta;

    // Flags
    private Boolean esServicio;
    private Boolean incluyeIva;
    private Boolean requiereInventario;
    private Boolean requiereReceta;
    private Boolean requierePersonalizacion;

    // Categorías e Impuestos
    private List<Long> categoriasIds;
    private List<CreateProductoImpuestoDTO> impuestos;
}