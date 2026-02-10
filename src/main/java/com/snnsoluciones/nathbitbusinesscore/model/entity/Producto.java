package com.snnsoluciones.nathbitbusinesscore.model.entity;

import com.snnsoluciones.nathbitbusinesscore.model.enums.TipoProducto;
import com.snnsoluciones.nathbitbusinesscore.model.enums.TipoInventario;
import com.snnsoluciones.nathbitbusinesscore.model.enums.ZonaPreparacion;
import com.snnsoluciones.nathbitbusinesscore.model.enums.UnidadMedida;
import com.snnsoluciones.nathbitbusinesscore.model.enums.mh.Moneda;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "productos", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo_interno"),
        @UniqueConstraint(columnNames = "nombre")
    },
    indexes = {
        @Index(name = "idx_productos_codigo_interno", columnList = "codigo_interno"),
        @Index(name = "idx_productos_codigo_barras", columnList = "codigo_barras"),
        @Index(name = "idx_productos_nombre", columnList = "nombre"),
        @Index(name = "idx_productos_tipo", columnList = "tipo"),
        @Index(name = "idx_productos_tipo_inventario", columnList = "tipo_inventario"),
        @Index(name = "idx_productos_activo", columnList = "activo")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== CÓDIGOS ====================
    
    @Column(name = "codigo_interno", nullable = false, length = 20)
    private String codigoInterno;

    @Column(name = "codigo_barras", length = 30)
    private String codigoBarras;

    // ==================== INFORMACIÓN BÁSICA ====================
    
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // ==================== RELACIONES (por ID, sin ManyToOne) ====================
    
    @Column(name = "empresa_cabys_id")
    private Long empresaCabysId;

    @Column(name = "familia_id")
    private Long familiaId;

    // ==================== CATEGORÍAS (ManyToMany funciona bien) ====================
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "producto_categoria",
        joinColumns = @JoinColumn(name = "producto_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private Set<CategoriaProducto> categorias = new HashSet<>();

    // ==================== TIPO Y CONTROL ====================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @Builder.Default
    private TipoProducto tipo = TipoProducto.VENTA;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inventario", nullable = false, length = 20)
    @Builder.Default
    private TipoInventario tipoInventario = TipoInventario.SIMPLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "zona_preparacion", nullable = false, length = 20)
    @Builder.Default
    private ZonaPreparacion zonaPreparacion = ZonaPreparacion.NINGUNA;

    // ==================== PRECIOS ====================
    
    @Column(name = "precio_venta", nullable = false, precision = 18, scale = 5)
    private BigDecimal precioVenta;

    @Column(name = "precio_base", precision = 18, scale = 5)
    private BigDecimal precioBase;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "ultimo_precio_compra", precision = 18, scale = 5)
    private BigDecimal ultimoPrecioCompra;

    // ==================== UNIDADES Y CONVERSIÓN ====================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 50)
    @Builder.Default
    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", nullable = false, length = 10)
    @Builder.Default
    private Moneda moneda = Moneda.CRC;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida_compra", length = 50)
    private UnidadMedida unidadMedidaCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida_uso", length = 50)
    private UnidadMedida unidadMedidaUso;

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

    @Column(name = "requiere_personalizacion", nullable = false)
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== MÉTODOS HELPER ====================

    /**
     * Verifica si el producto puede venderse directamente
     */
    public boolean esVendible() {
        return tipo == TipoProducto.VENTA ||
               tipo == TipoProducto.MIXTO ||
               tipo == TipoProducto.COMBO ||
               tipo == TipoProducto.COMPUESTO;
    }

    /**
     * Verifica si el producto puede usarse como ingrediente
     */
    public boolean esIngrediente() {
        return tipo == TipoProducto.MATERIA_PRIMA ||
               tipo == TipoProducto.MIXTO;
    }

    /**
     * Verifica si se produce con receta
     */
    public boolean seProduceConReceta() {
        return tipoInventario == TipoInventario.RECETA;
    }

    /**
     * Verifica si tiene inventario simple (compra/venta directa)
     */
    public boolean tieneInventarioSimple() {
        return tipoInventario == TipoInventario.SIMPLE;
    }

    /**
     * Verifica si es un combo
     */
    public boolean esCombo() {
        return tipo == TipoProducto.COMBO;
    }

    /**
     * Verifica si es un producto compuesto personalizable
     */
    public boolean esCompuesto() {
        return tipo == TipoProducto.COMPUESTO;
    }

    /**
     * Verifica si requiere preparación en alguna zona
     */
    public boolean requierePreparacion() {
        return zonaPreparacion != ZonaPreparacion.NINGUNA;
    }

    // ==================== LIFECYCLE CALLBACKS ====================

    @PrePersist
    protected void onCreate() {
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
        // updatedAt se maneja automáticamente con @UpdateTimestamp
    }
}