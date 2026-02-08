package com.snnsoluciones.nathbitbusinesscore.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Códigos CAByS - Catálogo global.
 * Tabla: public.codigos_cabys
 * 
 * IMPORTANTE: Esta tabla está en el schema PUBLIC (global),
 * no en el schema del tenant.
 */
@Entity
@Table(name = "codigos_cabys", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoCAByS {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo", nullable = false, unique = true, length = 13)
    private String codigo;
    
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "impuesto_sugerido", length = 100)
    private String impuestoSugerido;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}