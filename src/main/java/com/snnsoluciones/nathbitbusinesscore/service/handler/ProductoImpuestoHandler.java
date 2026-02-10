package com.snnsoluciones.nathbitbusinesscore.service.handler;

import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.CreateProductoImpuestoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.model.entity.ProductoImpuesto;
import com.snnsoluciones.nathbitbusinesscore.model.enums.mh.CodigoTarifaIVA;
import com.snnsoluciones.nathbitbusinesscore.model.enums.mh.TipoImpuesto;
import com.snnsoluciones.nathbitbusinesscore.repository.ProductoImpuestoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler para gestionar los impuestos de un producto
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoImpuestoHandler {

    private final ProductoImpuestoRepository impuestoRepository;
    private final EntityManager entityManager;

    /**
     * Asigna impuestos a un producto (creación)
     */
    /**
     * Asigna impuestos a un producto (creación)
     */
    public void asignarImpuestos(Producto producto, List<CreateProductoImpuestoDTO> impuestosDto) {
        if (impuestosDto == null || impuestosDto.isEmpty()) {
            log.debug("No hay impuestos para asignar al producto {}", producto.getCodigoInterno());
            return;
        }

        log.debug("💰 Asignando {} impuestos al producto {}", impuestosDto.size(), producto.getCodigoInterno());

        List<ProductoImpuesto> impuestos = new ArrayList<>();

        for (CreateProductoImpuestoDTO dto : impuestosDto) {
            validarImpuesto(dto);

            ProductoImpuesto impuesto = ProductoImpuesto.builder()
                .producto(producto)
                .tipoImpuesto(TipoImpuesto.valueOf(dto.getTipoImpuesto()))
                .codigoTarifaIva(CodigoTarifaIVA.valueOf(dto.getCodigoTarifaIva()))
                .porcentaje(dto.getPorcentaje())
                .activo(true)
                .build();

            impuestos.add(impuesto);
        }

        // ✅ Guardar directamente con el repository (sin producto.setImpuestos)
        impuestoRepository.saveAll(impuestos);

        log.debug("✅ Impuestos asignados correctamente");
    }

    /**
     * Actualiza los impuestos de un producto
     */
    public void actualizarImpuestos(Producto producto, List<CreateProductoImpuestoDTO> impuestosDto) {
        log.debug("🔄 Actualizando impuestos del producto {}", producto.getId());

        // Eliminar impuestos actuales
        impuestoRepository.deleteByProductoId(producto.getId());
        entityManager.flush(); // Forzar eliminación antes de crear nuevos

        // Asignar nuevos impuestos
        if (impuestosDto != null && !impuestosDto.isEmpty()) {
            List<ProductoImpuesto> nuevosImpuestos = new ArrayList<>();

            for (CreateProductoImpuestoDTO dto : impuestosDto) {
                validarImpuesto(dto);

                ProductoImpuesto impuesto = ProductoImpuesto.builder()
                    .producto(producto)
                    .tipoImpuesto(TipoImpuesto.valueOf(dto.getTipoImpuesto()))
                    .codigoTarifaIva(CodigoTarifaIVA.valueOf(dto.getCodigoTarifaIva()))
                    .porcentaje(dto.getPorcentaje())
                    .activo(true)
                    .build();

                nuevosImpuestos.add(impuesto);
            }

            impuestoRepository.saveAll(nuevosImpuestos);
            entityManager.flush(); // Forzar guardado

            log.debug("✅ {} impuestos actualizados", impuestosDto.size());
        } else {
            log.debug("✅ Impuestos eliminados (sin impuestos asignados)");
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Valida un impuesto
     */
    private void validarImpuesto(CreateProductoImpuestoDTO dto) {
        if (dto.getTipoImpuesto() == null || dto.getTipoImpuesto().isEmpty()) {
            throw new BusinessException("El tipo de impuesto es requerido");
        }

        if (dto.getCodigoTarifaIva() == null || dto.getCodigoTarifaIva().isEmpty()) {
            throw new BusinessException("El código de tarifa IVA es requerido");
        }

        if (dto.getPorcentaje() == null) {
            throw new BusinessException("El porcentaje de impuesto es requerido");
        }

        // Validar que el porcentaje sea válido (0-100)
        if (dto.getPorcentaje().doubleValue() < 0 || dto.getPorcentaje().doubleValue() > 100) {
            throw new BusinessException("El porcentaje debe estar entre 0 y 100");
        }
    }
}