package com.snnsoluciones.nathbitbusinesscore.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar EmpresaCabys.
 * Se usa cuando se asigna un código CAByS a la empresa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmpresaCabysDTO {

    @NotNull(message = "El ID del código CAByS es requerido")
    private Long codigoCabysId;

    @NotBlank(message = "El código es requerido")
    private String codigo;

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;

    private String impuestoSugerido;
}