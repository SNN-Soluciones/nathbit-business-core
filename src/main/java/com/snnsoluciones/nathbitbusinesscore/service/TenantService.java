package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.context.TenantContext;
import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import com.snnsoluciones.nathbitbusinesscore.model.dto.tenant.*;
import com.snnsoluciones.nathbitbusinesscore.model.entity.TenantConfiguracion;
import com.snnsoluciones.nathbitbusinesscore.model.entity.TenantInfo;
import com.snnsoluciones.nathbitbusinesscore.repository.TenantConfiguracionRepository;
import com.snnsoluciones.nathbitbusinesscore.repository.TenantInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantInfoRepository infoRepository;
    private final TenantConfiguracionRepository configuracionRepository;

    // ==================== TENANT INFO ====================

    @Transactional(readOnly = true)
    public TenantInfoDTO obtenerInfo() {
        String tenant = TenantContext.getCurrentTenant(); // ✅ Lo setea el interceptor
        log.debug("Obteniendo info para tenant: {}", tenant);

        TenantInfo info = infoRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(
                "No existe información del tenant"));

        return convertirInfoADto(info);
    }

    @Transactional
    public TenantInfoDTO actualizarInfo(UpdateTenantInfoDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Actualizando info para tenant: {}", tenant);

        TenantInfo info = infoRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(
                "No existe información del tenant"));

        // Actualizar campos
        if (dto.getNombre() != null) info.setNombre(dto.getNombre());
        if (dto.getNombreComercial() != null) info.setNombreComercial(dto.getNombreComercial());
        if (dto.getTelefono() != null) info.setTelefono(dto.getTelefono());
        if (dto.getEmail() != null) info.setEmail(dto.getEmail());
        if (dto.getProvinciaId() != null) info.setProvinciaId(dto.getProvinciaId());
        if (dto.getCantonId() != null) info.setCantonId(dto.getCantonId());
        if (dto.getDistritoId() != null) info.setDistritoId(dto.getDistritoId());
        if (dto.getBarrioId() != null) info.setBarrioId(dto.getBarrioId());
        if (dto.getOtrasSenas() != null) info.setOtrasSenas(dto.getOtrasSenas());

        TenantInfo guardado = infoRepository.save(info);
        log.info("✅ Info actualizada para tenant: {}", tenant);

        return convertirInfoADto(guardado);
    }

    // ==================== TENANT CONFIGURACION ====================

    @Transactional(readOnly = true)
    public TenantConfiguracionDTO obtenerConfiguracion() {
        String tenant = TenantContext.getCurrentTenant();
        log.debug("Obteniendo configuración para tenant: {}", tenant);

        TenantConfiguracion config = configuracionRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(
                "No existe configuración del tenant"));

        return convertirConfiguracionADto(config);
    }

    @Transactional
    public TenantConfiguracionDTO actualizarConfiguracion(UpdateTenantConfiguracionDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Actualizando configuración para tenant: {}", tenant);

        TenantConfiguracion config = configuracionRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(
                "No existe configuración del tenant"));

        // Actualizar campos de facturación
        if (dto.getModoFacturacion() != null) {
            config.setModoFacturacion(
                TenantConfiguracion.ModoFacturacion.valueOf(dto.getModoFacturacion()));
        }
        if (dto.getRegimenTributario() != null) {
            config.setRegimenTributario(
                TenantConfiguracion.RegimenTributario.valueOf(dto.getRegimenTributario()));
        }
        if (dto.getRequiereHacienda() != null) {
            config.setRequiereHacienda(dto.getRequiereHacienda());
        }

        // Actualizar campos de inventario
        if (dto.getManejaInventario() != null) {
            config.setManejaInventario(dto.getManejaInventario());
        }
        if (dto.getAplicaRecetas() != null) {
            config.setAplicaRecetas(dto.getAplicaRecetas());
        }
        if (dto.getPermiteNegativos() != null) {
            config.setPermiteNegativos(dto.getPermiteNegativos());
        }

        // Actualizar campos de impresión
        if (dto.getModoImpresion() != null) {
            config.setModoImpresion(
                TenantConfiguracion.ModoImpresion.valueOf(dto.getModoImpresion()));
        }
        if (dto.getMetodoImpresion() != null) {
            config.setMetodoImpresion(
                TenantConfiguracion.MetodoImpresion.valueOf(dto.getMetodoImpresion()));
        }
        if (dto.getIpOrquestador() != null) {
            config.setIpOrquestador(dto.getIpOrquestador());
        }
        if (dto.getImpresionAutomatica() != null) {
            config.setImpresionAutomatica(dto.getImpresionAutomatica());
        }
        if (dto.getAutoImprimirFactura() != null) {
            config.setAutoImprimirFactura(dto.getAutoImprimirFactura());
        }
        if (dto.getAutoImprimirComanda() != null) {
            config.setAutoImprimirComanda(dto.getAutoImprimirComanda());
        }
        if (dto.getTiempoAutoClose() != null) {
            config.setTiempoAutoClose(dto.getTiempoAutoClose());
        }

        // Actualizar estado
        if (dto.getActiva() != null) {
            config.setActiva(dto.getActiva());
        }

        TenantConfiguracion guardado = configuracionRepository.save(config);
        log.info("✅ Configuración actualizada para tenant: {}", tenant);

        return convertirConfiguracionADto(guardado);
    }

    // ==================== HELPERS ====================

    /**
     * Obtiene nombre comercial para carpetas de imágenes
     */
    public String obtenerNombreComercial() {
        TenantInfo info = infoRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException("No existe información del tenant"));

        return info.getNombreComercial() != null
            ? info.getNombreComercial()
            : info.getNombre();
    }

    /**
     * Verifica si usa régimen simplificado
     */
    public boolean esRegimenSimplificado() {
        TenantConfiguracion config = configuracionRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException("No existe configuración del tenant"));

        return config.getRegimenTributario() ==
            TenantConfiguracion.RegimenTributario.REGIMEN_SIMPLIFICADO;
    }

    /**
     * Verifica si requiere facturación electrónica
     */
    public boolean requiereFacturacionElectronica() {
        TenantConfiguracion config = configuracionRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException("No existe configuración del tenant"));

        return config.getModoFacturacion() ==
            TenantConfiguracion.ModoFacturacion.ELECTRONICO;
    }

    // ==================== CONVERSORES ====================

    private TenantInfoDTO convertirInfoADto(TenantInfo info) {
        return TenantInfoDTO.builder()
            .id(info.getId())
            .codigoTenant(info.getCodigoTenant())
            .nombre(info.getNombre())
            .nombreComercial(info.getNombreComercial())
            .telefono(info.getTelefono())
            .email(info.getEmail())
            .provinciaId(info.getProvinciaId())
            .cantonId(info.getCantonId())
            .distritoId(info.getDistritoId())
            .barrioId(info.getBarrioId())
            .otrasSenas(info.getOtrasSenas())
            .logoUrl(info.getLogoUrl())
            .logoKey(info.getLogoKey())
            .createdAt(info.getCreatedAt())
            .updatedAt(info.getUpdatedAt())
            .build();
    }

    private TenantConfiguracionDTO convertirConfiguracionADto(TenantConfiguracion config) {
        return TenantConfiguracionDTO.builder()
            .id(config.getId())
            .codigoTenant(config.getCodigoTenant())
            .modoFacturacion(config.getModoFacturacion().name())
            .regimenTributario(config.getRegimenTributario().name())
            .requiereHacienda(config.getRequiereHacienda())
            .manejaInventario(config.getManejaInventario())
            .aplicaRecetas(config.getAplicaRecetas())
            .permiteNegativos(config.getPermiteNegativos())
            .modoImpresion(config.getModoImpresion().name())
            .metodoImpresion(config.getMetodoImpresion().name())
            .ipOrquestador(config.getIpOrquestador())
            .impresionAutomatica(config.getImpresionAutomatica())
            .autoImprimirFactura(config.getAutoImprimirFactura())
            .autoImprimirComanda(config.getAutoImprimirComanda())
            .tiempoAutoClose(config.getTiempoAutoClose())
            .activa(config.getActiva())
            .createdAt(config.getCreatedAt())
            .updatedAt(config.getUpdatedAt())
            .build();
    }
}