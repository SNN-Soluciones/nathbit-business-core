package com.snnsoluciones.nathbitbusinesscore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para CodigoCabys (catálogo global)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoCabysDTO {
    private Long id;
    private String codigo;
    private String descripcion;
    private String impuestoSugerido;
    private Boolean activo;
}