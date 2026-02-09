package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.model.dto.CodigoCabysDTO;
import com.snnsoluciones.nathbitbusinesscore.model.entity.CodigoCAByS;
import com.snnsoluciones.nathbitbusinesscore.repository.CodigoCabysRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar el catálogo global de códigos CAByS.
 * 
 * IMPORTANTE: Esta tabla está en public (NO usa multi-tenancy).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodigoCabysService {

    private final CodigoCabysRepository codigoCabysRepository;

    /**
     * Buscar códigos CAByS con filtros opcionales
     * 
     * @param descripcion Filtro por descripción (parcial, case-insensitive)
     * @param codigo Filtro por código (prefijo)
     * @param impuesto Filtro por impuesto sugerido (exacto)
     * @return Lista de códigos que cumplen los filtros
     */
    @Transactional(readOnly = true)
    public List<CodigoCabysDTO> buscarCabys(String descripcion, String codigo, String impuesto) {
        log.info("🔍 Buscando CAByS - descripcion: {}, codigo: {}, impuesto: {}", 
                 descripcion, codigo, impuesto);

        List<CodigoCAByS> resultados = codigoCabysRepository.buscarConFiltros(
            descripcion, 
            codigo, 
            impuesto
        );

        log.info("✅ Encontrados {} códigos CAByS", resultados.size());

        return resultados.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    /**
     * Convierte entidad a DTO
     */
    private CodigoCabysDTO convertirADTO(CodigoCAByS entity) {
        return CodigoCabysDTO.builder()
            .id(entity.getId())
            .codigo(entity.getCodigo())
            .descripcion(entity.getDescripcion())
            .impuestoSugerido(entity.getImpuestoSugerido())
            .activo(entity.getActivo())
            .build();
    }
}