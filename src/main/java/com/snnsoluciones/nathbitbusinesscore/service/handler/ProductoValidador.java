package com.snnsoluciones.nathbitbusinesscore.service.handler;

import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.CreateProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.UpdateProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validador de reglas de negocio para productos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoValidador {

    private final ProductoRepository productoRepository;

    /**
     * Valida datos para crear un producto
     */
    public void validarCreacion(CreateProductoDTO dto) {
        log.debug("🔍 Validando creación de producto: {}", dto.getCodigoInterno());

        // Validar código interno único
        validarCodigoInternoUnico(dto.getCodigoInterno(), null);

        // Validar código de barras único (si existe)
        if (dto.getCodigoBarras() != null && !dto.getCodigoBarras().isEmpty()) {
            validarCodigoBarrasUnico(dto.getCodigoBarras(), null);
        }

        // Validar precios
        validarPrecios(dto.getPrecioVenta(), dto.getPrecioBase(), dto.getPrecioCompra());

        // Validar tipo de inventario vs requiere inventario
        validarInventario(dto.getTipoInventario(), dto.getRequiereInventario());

        // Validar factores de conversión
        validarFactoresConversion(dto.getFactorConversion(), dto.getFactorConversionReceta());

        log.debug("✅ Validación de creación exitosa");
    }

    /**
     * Valida datos para actualizar un producto
     */
    public void validarActualizacion(Long productoId, UpdateProductoDTO dto) {
        log.debug("🔍 Validando actualización de producto ID: {}", productoId);

        // Validar que el producto existe
        Producto productoExistente = productoRepository.findById(productoId)
            .orElseThrow(() -> new BusinessException("Producto no encontrado con ID: " + productoId));

        // Validar código interno único (si cambió)
        if (dto.getCodigoInterno() != null) {
            validarCodigoInternoUnico(dto.getCodigoInterno(), productoId);
        }

        // Validar código de barras único (si cambió)
        if (dto.getCodigoBarras() != null && !dto.getCodigoBarras().isEmpty()) {
            validarCodigoBarrasUnico(dto.getCodigoBarras(), productoId);
        }

        // Validar precios
        BigDecimal precioVenta = dto.getPrecioVenta() != null ? dto.getPrecioVenta() : productoExistente.getPrecioVenta();
        BigDecimal precioBase = dto.getPrecioBase() != null ? dto.getPrecioBase() : productoExistente.getPrecioBase();
        BigDecimal precioCompra = dto.getPrecioCompra() != null ? dto.getPrecioCompra() : productoExistente.getPrecioCompra();
        validarPrecios(precioVenta, precioBase, precioCompra);

        // Validar tipo de inventario vs requiere inventario
        String tipoInventario = dto.getTipoInventario() != null ? dto.getTipoInventario() : productoExistente.getTipoInventario().name();
        Boolean requiereInventario = dto.getRequiereInventario() != null ? dto.getRequiereInventario() : productoExistente.getRequiereInventario();
        validarInventario(tipoInventario, requiereInventario);

        log.debug("✅ Validación de actualización exitosa");
    }

    // ==================== VALIDACIONES ESPECÍFICAS ====================

    /**
     * Valida que el código interno sea único
     */
    private void validarCodigoInternoUnico(String codigoInterno, Long productoIdExcluir) {
        boolean existe = productoIdExcluir != null
            ? productoRepository.existsByCodigoInternoAndIdNot(codigoInterno, productoIdExcluir)
            : productoRepository.existsByCodigoInterno(codigoInterno);

        if (existe) {
            throw new BusinessException("Ya existe un producto con el código interno: " + codigoInterno);
        }
    }

    /**
     * Valida que el código de barras sea único
     */
    private void validarCodigoBarrasUnico(String codigoBarras, Long productoIdExcluir) {
        boolean existe = productoIdExcluir != null
            ? productoRepository.existsByCodigoBarrasAndIdNot(codigoBarras, productoIdExcluir)
            : productoRepository.existsByCodigoBarras(codigoBarras);

        if (existe) {
            throw new BusinessException("Ya existe un producto con el código de barras: " + codigoBarras);
        }
    }

    /**
     * Valida que los precios sean coherentes
     */
    private void validarPrecios(BigDecimal precioVenta, BigDecimal precioBase, BigDecimal precioCompra) {
        // Precio de venta debe ser mayor a 0
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio de venta debe ser mayor a 0");
        }

        // Si hay precio base, debe ser menor o igual al precio de venta
        if (precioBase != null && precioBase.compareTo(precioVenta) > 0) {
            throw new BusinessException("El precio base no puede ser mayor al precio de venta");
        }

        // Si hay precio de compra, debe ser positivo
        if (precioCompra != null && precioCompra.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El precio de compra no puede ser negativo");
        }
    }

    /**
     * Valida coherencia entre tipo de inventario y requiere inventario
     */
    private void validarInventario(String tipoInventario, Boolean requiereInventario) {
        if (tipoInventario == null || requiereInventario == null) {
            return;
        }

        // Si tipo es NO_APLICA, entonces NO debe requerir inventario
        if ("NO_APLICA".equals(tipoInventario) && requiereInventario) {
            throw new BusinessException(
                "Un producto con tipo de inventario NO_APLICA no puede requerir inventario"
            );
        }

        // Si tipo es SIMPLE o COMPUESTO, debe requerir inventario
        if (("SIMPLE".equals(tipoInventario) || "COMPUESTO".equals(tipoInventario)) && !requiereInventario) {
            throw new BusinessException(
                "Un producto con tipo de inventario " + tipoInventario + " debe requerir inventario"
            );
        }
    }

    /**
     * Valida factores de conversión
     */
    private void validarFactoresConversion(BigDecimal factorConversion, BigDecimal factorConversionReceta) {
        // Si hay factor de conversión, debe ser mayor a 0
        if (factorConversion != null && factorConversion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El factor de conversión debe ser mayor a 0");
        }

        // Factor de conversión de receta debe ser mayor a 0
        if (factorConversionReceta != null && factorConversionReceta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El factor de conversión de receta debe ser mayor a 0");
        }
    }
}