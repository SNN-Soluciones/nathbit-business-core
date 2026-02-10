package com.snnsoluciones.nathbitbusinesscore.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Información básica del tenant (datos estables)
 * Solo debe haber 1 registro por base de datos
 */
@Entity
@Table(name = "tenant_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificación
    @Column(nullable = false, unique = true, length = 50)
    private String codigoTenant; // tenant_001, tenant_002, etc.

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 100)
    private String nombreComercial;

    // Contacto
    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    // Ubicación
    @Column(length = 10)
    private String provinciaId;

    @Column(length = 10)
    private String cantonId;

    @Column(length = 10)
    private String distritoId;

    @Column(length = 10)
    private String barrioId;

    @Column(length = 500)
    private String otrasSenas;

    // Imagen
    @Column(length = 255)
    private String logoUrl;

    @Column(length = 255)
    private String logoKey;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}