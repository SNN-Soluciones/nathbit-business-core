package com.snnsoluciones.nathbitbusinesscore.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar CategoriaProducto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoriaProductoDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    @Size(max = 7, message = "El color debe ser un código hexadecimal (#RRGGBB)")
    private String color;

    @Size(max = 50, message = "El icono no puede exceder 50 caracteres")
    private String icono;

    @NotNull(message = "El orden es requerido")
    @Min(value = 0, message = "El orden debe ser mayor o igual a 0")
    private Integer orden;
}