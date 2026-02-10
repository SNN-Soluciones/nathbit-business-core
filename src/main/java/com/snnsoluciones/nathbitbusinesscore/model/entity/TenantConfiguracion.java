package com.snnsoluciones.nathbitbusinesscore.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Configuración dinámica del tenant
 * Solo debe haber 1 registro por base de datos
 */
@Entity
@Table(name = "tenant_configuracion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantConfiguracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigoTenant;

    // ==================== FACTURACIÓN ====================
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModoFacturacion modoFacturacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegimenTributario regimenTributario;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requiereHacienda = false;

    // ==================== INVENTARIO ====================
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean manejaInventario = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aplicaRecetas = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean permiteNegativos = false;

    // ==================== IMPRESIÓN ====================
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ModoImpresion modoImpresion = ModoImpresion.LOCAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MetodoImpresion metodoImpresion = MetodoImpresion.AUTO;

    @Column(length = 100)
    private String ipOrquestador;

    @Column(nullable = false)
    @Builder.Default
    private Boolean impresionAutomatica = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoImprimirFactura = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoImprimirComanda = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer tiempoAutoClose = 2;

    // ==================== ESTADO ====================
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ==================== ENUMS ====================
    
    public enum ModoFacturacion {
        SOLO_INTERNO,
        MIXTO,
        ELECTRONICO
    }

    public enum RegimenTributario {
        REGIMEN_SIMPLIFICADO,
        REGIMEN_TRADICIONAL
    }

    public enum ModoImpresion {
        LOCAL,
        ORQUESTADOR
    }

    public enum MetodoImpresion {
        AUTO,
        IFRAME,
        SHARE_API,
        NUEVA_PESTANA
    }

    // ==================== MÉTODOS HELPER ====================
    
    public boolean puedeVenderProductoConReceta() {
        return this.aplicaRecetas;
    }

    public boolean requiereValidarStock() {
        return this.manejaInventario && !this.permiteNegativos;
    }

    public boolean puedeVenderSinStock() {
        return !this.manejaInventario || this.permiteNegativos;
    }

    public boolean usaImpresionLocal() {
        return this.modoImpresion == ModoImpresion.LOCAL;
    }

    public boolean usaOrquestador() {
        return this.modoImpresion == ModoImpresion.ORQUESTADOR;
    }

    public String getUrlOrquestador() {
        if (this.ipOrquestador == null || this.ipOrquestador.trim().isEmpty()) {
            return null;
        }

        String ip = this.ipOrquestador.trim();

        if (ip.startsWith("http://") || ip.startsWith("https://")) {
            return ip;
        }

        return "http://" + ip;
    }

    public boolean tieneConfiguracionImpresionValida() {
        if (this.modoImpresion == ModoImpresion.ORQUESTADOR) {
            return this.ipOrquestador != null && !this.ipOrquestador.trim().isEmpty();
        }
        return true;
    }
}