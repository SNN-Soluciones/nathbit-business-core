package com.snnsoluciones.nathbitbusinesscore.model.dto;

import com.snnsoluciones.nathbitbusinesscore.model.entity.EmpresaCabys;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de EmpresaCabys.
 * Los datos del CAByS están duplicados en la misma tabla (sin JOIN).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaCabysDTO {

    private Long id;
    private Long codigoCabysId;
    
    // Datos del CAByS (duplicados en la tabla)
    private String codigo;
    private String descripcion;
    private String impuestoSugerido;
    
    private Boolean activo;
    private LocalDateTime createdAt;

    /**
     * Convierte una entidad a DTO.
     * Simple mapeo 1:1, sin JOINs.
     */
    public static EmpresaCabysDTO fromEntity(EmpresaCabys entity) {
        if (entity == null) {
            return null;
        }
        
        return EmpresaCabysDTO.builder()
                .id(entity.getId())
                .codigoCabysId(entity.getCodigoCabysId())
                .codigo(entity.getCodigo())
                .descripcion(entity.getDescripcion())
                .impuestoSugerido(entity.getImpuestoSugerido())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}