package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

import com.snnsoluciones.nathbitbusinesscore.model.dto.CategoriaProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO completo para respuesta de Producto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private Long id;
    
    // Códigos
    private String codigoInterno;
    private String codigoBarras;
    
    // Info básica
    private String nombre;
    private String descripcion;
    
    // Relaciones
    private Long empresaCabysId;
    private Long familiaId;
    
    // Tipo y control
    private String tipo;
    private String tipoInventario;
    private String zonaPreparacion;
    
    // Precios
    private BigDecimal precioVenta;
    private BigDecimal precioBase;
    private BigDecimal precioCompra;
    private BigDecimal ultimoPrecioCompra;
    
    // Unidades
    private String unidadMedida;
    private String moneda;
    private String unidadMedidaCompra;
    private String unidadMedidaUso;
    
    // Factores
    private BigDecimal factorConversion;
    private BigDecimal factorConversionReceta;
    
    // Flags
    private Boolean activo;
    private Boolean esServicio;
    private Boolean incluyeIva;
    private Boolean requiereInventario;
    private Boolean requiereReceta;
    private Boolean requierePersonalizacion;
    
    // Imágenes
    private String imagenUrl;
    private String imagenKey;
    private String thumbnailUrl;
    private String thumbnailKey;
    
    // Fechas
    private LocalDateTime fechaUltimaCompra;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relaciones (anidadas)
    private List<CategoriaProductoDTO> categorias;
    private List<ProductoImpuestoDTO> impuestos;

    /**
     * Convierte una entidad a DTO completo
     */
    public static ProductoDTO fromEntity(Producto entity) {
        if (entity == null) {
            return null;
        }
        
        return ProductoDTO.builder()
                .id(entity.getId())
                .codigoInterno(entity.getCodigoInterno())
                .codigoBarras(entity.getCodigoBarras())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .empresaCabysId(entity.getEmpresaCabysId())
                .familiaId(entity.getFamiliaId())
                .tipo(entity.getTipo())
                .tipoInventario(entity.getTipoInventario())
                .zonaPreparacion(entity.getZonaPreparacion())
                .precioVenta(entity.getPrecioVenta())
                .precioBase(entity.getPrecioBase())
                .precioCompra(entity.getPrecioCompra())
                .ultimoPrecioCompra(entity.getUltimoPrecioCompra())
                .unidadMedida(entity.getUnidadMedida())
                .moneda(entity.getMoneda())
                .unidadMedidaCompra(entity.getUnidadMedidaCompra())
                .unidadMedidaUso(entity.getUnidadMedidaUso())
                .factorConversion(entity.getFactorConversion())
                .factorConversionReceta(entity.getFactorConversionReceta())
                .activo(entity.getActivo())
                .esServicio(entity.getEsServicio())
                .incluyeIva(entity.getIncluyeIva())
                .requiereInventario(entity.getRequiereInventario())
                .requiereReceta(entity.getRequiereReceta())
                .requierePersonalizacion(entity.getRequierePersonalizacion())
                .imagenUrl(entity.getImagenUrl())
                .imagenKey(entity.getImagenKey())
                .thumbnailUrl(entity.getThumbnailUrl())
                .thumbnailKey(entity.getThumbnailKey())
                .fechaUltimaCompra(entity.getFechaUltimaCompra())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .categorias(entity.getCategorias() != null 
                    ? entity.getCategorias().stream()
                        .map(CategoriaProductoDTO::fromEntity)
                        .collect(Collectors.toList())
                    : null)
                .impuestos(entity.getImpuestos() != null
                    ? entity.getImpuestos().stream()
                        .map(ProductoImpuestoDTO::fromEntity)
                        .collect(Collectors.toList())
                    : null)
                .build();
    }
}