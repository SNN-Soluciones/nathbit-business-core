package com.snnsoluciones.nathbitbusinesscore.model.dto;

import com.snnsoluciones.nathbitbusinesscore.model.entity.CategoriaProducto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de CategoriaProducto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String color;
    private String icono;
    private Integer orden;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad a DTO
     */
    public static CategoriaProductoDTO fromEntity(CategoriaProducto entity) {
        if (entity == null) {
            return null;
        }
        
        return CategoriaProductoDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .color(entity.getColor())
                .icono(entity.getIcono())
                .orden(entity.getOrden())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}