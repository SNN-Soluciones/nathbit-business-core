package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear/asignar impuesto a producto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductoImpuestoDTO {

    @NotBlank(message = "El tipo de impuesto es obligatorio")
    private String tipoImpuesto;

    private String codigoTarifaIva;

    @NotNull(message = "El porcentaje es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje debe ser mayor o igual a 0")
    private BigDecimal porcentaje;
}