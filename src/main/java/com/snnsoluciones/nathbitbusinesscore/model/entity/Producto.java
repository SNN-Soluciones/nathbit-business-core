package com.snnsoluciones.nathbitbusinesscore.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Productos del tenant.
 * Tabla: tenant_X.productos
 * 
 * Requiere:
 * - Device Token: Para acceso al tenant
 * - Bearer Token: Para auditar quién crea/modifica
 */
@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_productos_codigo_interno", columnList = "codigo_interno"),
    @Index(name = "idx_productos_codigo_barras", columnList = "codigo_barras"),
    @Index(name = "idx_productos_nombre", columnList = "nombre"),
    @Index(name = "idx_productos_tipo", columnList = "tipo"),
    @Index(name = "idx_productos_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== CÓDIGOS ====================
    
    @Column(name = "codigo_interno", nullable = false, length = 20)
    private String codigoInterno;

    @Column(name = "codigo_barras", length = 30)
    private String codigoBarras;

    // ==================== INFO BÁSICA ====================
    
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // ==================== RELACIONES ====================
    
    @Column(name = "empresa_cabys_id")
    private Long empresaCabysId;

    @Column(name = "familia_id")
    private Long familiaId;

    // ==================== TIPO Y CONTROL ====================
    
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo; // VENTA, MATERIA_PRIMA, MIXTO, COMBO, COMPUESTO

    @Column(name = "tipo_inventario", nullable = false, length = 50)
    private String tipoInventario; // SIMPLE, RECETA, NINGUNO

    @Column(name = "zona_preparacion", nullable = false, length = 50)
    private String zonaPreparacion; // NINGUNA, COCINA, BAR, etc

    // ==================== PRECIOS ====================
    
    @Column(name = "precio_venta", nullable = false, precision = 18, scale = 5)
    private BigDecimal precioVenta;

    @Column(name = "precio_base", precision = 18, scale = 5)
    private BigDecimal precioBase;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "ultimo_precio_compra", precision = 18, scale = 5)
    private BigDecimal ultimoPrecioCompra;

    // ==================== UNIDADES ====================
    
    @Column(name = "unidad_medida", nullable = false, length = 255)
    private String unidadMedida;

    @Column(name = "moneda", nullable = false, length = 255)
    private String moneda;

    @Column(name = "unidad_medida_compra", length = 50)
    private String unidadMedidaCompra;

    @Column(name = "unidad_medida_uso", length = 50)
    private String unidadMedidaUso;

    // ==================== FACTORES ====================
    
    @Column(name = "factor_conversion", precision = 10, scale = 4)
    private BigDecimal factorConversion;

    @Column(name = "factor_conversion_receta", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal factorConversionReceta = BigDecimal.ONE;

    // ==================== FLAGS ====================
    
    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "es_servicio", nullable = false)
    @Builder.Default
    private Boolean esServicio = false;

    @Column(name = "incluye_iva", nullable = false)
    @Builder.Default
    private Boolean incluyeIva = true;

    @Column(name = "requiere_inventario", nullable = false)
    @Builder.Default
    private Boolean requiereInventario = false;

    @Column(name = "requiere_receta", nullable = false)
    @Builder.Default
    private Boolean requiereReceta = false;

    @Column(name = "requiere_personalizacion")
    @Builder.Default
    private Boolean requierePersonalizacion = false;

    // ==================== IMÁGENES ====================
    
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "imagen_key", length = 255)
    private String imagenKey;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "thumbnail_key", length = 255)
    private String thumbnailKey;

    // ==================== FECHAS ====================
    
    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== RELACIONES MANY-TO-MANY ====================
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "producto_categoria",
        joinColumns = @JoinColumn(name = "producto_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private Set<CategoriaProducto> categorias = new HashSet<>();

    @OneToMany(mappedBy = "productoId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ProductoImpuesto> impuestos = new HashSet<>();

    // ==================== LIFECYCLE ====================
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        if (activo == null) activo = true;
        if (esServicio == null) esServicio = false;
        if (incluyeIva == null) incluyeIva = true;
        if (requiereInventario == null) requiereInventario = false;
        if (requiereReceta == null) requiereReceta = false;
        if (requierePersonalizacion == null) requierePersonalizacion = false;
        if (factorConversionReceta == null) factorConversionReceta = BigDecimal.ONE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}