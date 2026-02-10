package com.snnsoluciones.nathbitbusinesscore.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Códigos CAByS asignados a este tenant.
 * Tabla: tenant_X.empresa_cabys
 * 
 * NOTA: Los datos del CAByS (codigo, descripcion, impuesto_sugerido)
 * están DUPLICADOS aquí para evitar JOINs complicados con public.codigos_cabys.
 * 
 * Requiere:
 * - Device Token: Para acceso al tenant
 * - Bearer Token: Para auditar quién asigna códigos CAByS
 */
@Entity
@Table(name = "empresa_cabys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaCabys {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al ID original en public.codigos_cabys (por si acaso)
    @Column(name = "codigo_cabys_id", nullable = false)
    private Long codigoCabysId;

    // Datos del CAByS DUPLICADOS (sin relación @ManyToOne)
    @Column(name = "codigo", nullable = false, length = 13)
    private String codigo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "impuesto_sugerido", length = 100)
    private String impuestoSugerido;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Se ejecuta antes de persistir (INSERT)
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        
        if (activo == null) {
            activo = true;
        }
    }
}