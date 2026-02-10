package com.snnsoluciones.nathbitbusinesscore.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Categorías de productos del tenant.
 * Tabla: tenant_X.categorias_producto
 * 
 * Requiere:
 * - Device Token: Para acceso al tenant
 * - Bearer Token: Para auditar quién crea/modifica
 */
@Entity
@Table(name = "categorias_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "icono", length = 50)
    private String icono;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Se ejecuta antes de persistir (INSERT)
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        if (activo == null) {
            activo = true;
        }
    }

    /**
     * Se ejecuta antes de actualizar (UPDATE)
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}