package com.snnsoluciones.nathbitbusinesscore.service.handler;

import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.CreateProductoDTO;
import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.CreateProductoImpuestoDTO;
import com.snnsoluciones.nathbitbusinesscore.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler para gestionar tributación según régimen (simplificado vs tradicional)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoTributacionHandler {

    private final TenantService tenantService;

    /**
     * Configura impuestos según el régimen tributario del tenant
     * - Régimen Simplificado: IVA exento (tarifa 01)
     * - Régimen Tradicional: Usa los impuestos definidos en el DTO
     */
    public void configurarImpuestosSegunRegimen(CreateProductoDTO dto) {
        boolean esRegimenSimplificado = tenantService.esRegimenSimplificado();

        log.debug("🏛️ Configurando impuestos - Régimen: {}", 
            esRegimenSimplificado ? "SIMPLIFICADO" : "TRADICIONAL");

        if (esRegimenSimplificado) {
            // Régimen Simplificado: Forzar IVA exento
            configurarIvaExento(dto);
        } else {
            // Régimen Tradicional: Validar que tenga impuestos
            validarImpuestosTradicional(dto);
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Configura IVA exento para régimen simplificado
     * Tarifa 01: Tarifa 0% (Exento Art. 32)
     */
    private void configurarIvaExento(CreateProductoDTO dto) {
        log.debug("📝 Aplicando IVA exento (Régimen Simplificado)");

        // Forzar incluye IVA en false
        dto.setIncluyeIva(false);

        // Crear impuesto exento
        List<CreateProductoImpuestoDTO> impuestos = new ArrayList<>();

        CreateProductoImpuestoDTO ivaExento = CreateProductoImpuestoDTO.builder()
            .tipoImpuesto("IVA")
            .codigoTarifaIva("01") // Tarifa 0% (Exento Art. 32)
            .porcentaje(BigDecimal.ZERO)
            .build();

        impuestos.add(ivaExento);
        dto.setImpuestos(impuestos);

        log.debug("✅ IVA exento configurado");
    }

    /**
     * Valida que en régimen tradicional tenga impuestos definidos
     */
    private void validarImpuestosTradicional(CreateProductoDTO dto) {
        log.debug("📝 Validando impuestos (Régimen Tradicional)");

        // En régimen tradicional, si incluye IVA debe tener impuestos definidos
        if (dto.getIncluyeIva() && (dto.getImpuestos() == null || dto.getImpuestos().isEmpty())) {
            log.warn("⚠️ Producto incluye IVA pero no tiene impuestos definidos - se debe configurar manualmente");
        }

        log.debug("✅ Validación de impuestos completada");
    }
}