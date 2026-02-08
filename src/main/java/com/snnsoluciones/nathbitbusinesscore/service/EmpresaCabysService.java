package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.context.TenantContext;
import com.snnsoluciones.nathbitbusinesscore.model.dto.CreateEmpresaCabysDTO;
import com.snnsoluciones.nathbitbusinesscore.model.dto.EmpresaCabysDTO;
import com.snnsoluciones.nathbitbusinesscore.model.entity.EmpresaCabys;
import com.snnsoluciones.nathbitbusinesscore.repository.EmpresaCabysRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Códigos CAByS de la Empresa.
 * Requiere tenant configurado en TenantContext.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaCabysService {

    private final EmpresaCabysRepository empresaCabysRepository;

    /**
     * Obtener todos los CAByS activos de la empresa
     */
    @Transactional(readOnly = true)
    public List<EmpresaCabysDTO> obtenerCabysActivos() {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo CAByS activos para tenant: {}", tenant);
        
        List<EmpresaCabys> cabys = empresaCabysRepository.findByActivoTrue();
        
        log.debug("Se encontraron {} códigos CAByS activos", cabys.size());
        
        return cabys.stream()
                .map(EmpresaCabysDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los CAByS (activos e inactivos)
     */
    @Transactional(readOnly = true)
    public List<EmpresaCabysDTO> obtenerTodosCabys() {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo todos los CAByS para tenant: {}", tenant);
        
        List<EmpresaCabys> cabys = empresaCabysRepository.findAll();
        
        log.debug("Se encontraron {} códigos CAByS en total", cabys.size());
        
        return cabys.stream()
                .map(EmpresaCabysDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un CAByS por ID
     */
    @Transactional(readOnly = true)
    public EmpresaCabysDTO obtenerCabysPorId(Long id) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Obteniendo CAByS con ID {} para tenant: {}", id, tenant);
        
        return empresaCabysRepository.findById(id)
                .map(EmpresaCabysDTO::fromEntity)
                .orElse(null);
    }

    /**
     * Crear/Asignar un nuevo código CAByS a la empresa
     */
    @Transactional
    public EmpresaCabysDTO crearCabys(CreateEmpresaCabysDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Creando nuevo CAByS para tenant: {}", tenant);
        
        // Verificar si ya existe
        if (empresaCabysRepository.existsByCodigoCabysId(dto.getCodigoCabysId())) {
            log.warn("El código CAByS ID {} ya está asignado a este tenant", dto.getCodigoCabysId());
            throw new IllegalArgumentException("Este código CAByS ya está asignado a la empresa");
        }
        
        // Crear la entidad
        EmpresaCabys empresaCabys = EmpresaCabys.builder()
                .codigoCabysId(dto.getCodigoCabysId())
                .codigo(dto.getCodigo())
                .descripcion(dto.getDescripcion())
                .impuestoSugerido(dto.getImpuestoSugerido())
                .activo(true)
                .build();
        
        EmpresaCabys saved = empresaCabysRepository.save(empresaCabys);
        
        log.info("CAByS creado exitosamente con ID: {}", saved.getId());
        
        return EmpresaCabysDTO.fromEntity(saved);
    }

    /**
     * Actualizar un código CAByS
     */
    @Transactional
    public EmpresaCabysDTO actualizarCabys(Long id, CreateEmpresaCabysDTO dto) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Actualizando CAByS con ID {} para tenant: {}", id, tenant);
        
        EmpresaCabys empresaCabys = empresaCabysRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CAByS no encontrado con ID: " + id));
        
        // Actualizar campos
        empresaCabys.setCodigoCabysId(dto.getCodigoCabysId());
        empresaCabys.setCodigo(dto.getCodigo());
        empresaCabys.setDescripcion(dto.getDescripcion());
        empresaCabys.setImpuestoSugerido(dto.getImpuestoSugerido());
        
        EmpresaCabys updated = empresaCabysRepository.save(empresaCabys);
        
        log.info("CAByS actualizado exitosamente");
        
        return EmpresaCabysDTO.fromEntity(updated);
    }

    /**
     * Activar/Desactivar un código CAByS
     */
    @Transactional
    public EmpresaCabysDTO cambiarEstadoCabys(Long id, boolean activo) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Cambiando estado de CAByS con ID {} a {} para tenant: {}", id, activo, tenant);
        
        EmpresaCabys empresaCabys = empresaCabysRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CAByS no encontrado con ID: " + id));
        
        empresaCabys.setActivo(activo);
        
        EmpresaCabys updated = empresaCabysRepository.save(empresaCabys);
        
        log.info("Estado de CAByS cambiado exitosamente a: {}", activo);
        
        return EmpresaCabysDTO.fromEntity(updated);
    }

    /**
     * Eliminar un código CAByS (soft delete - desactivar)
     */
    @Transactional
    public void eliminarCabys(Long id) {
        String tenant = TenantContext.getCurrentTenant();
        log.info("Eliminando (desactivando) CAByS con ID {} para tenant: {}", id, tenant);
        
        cambiarEstadoCabys(id, false);
        
        log.info("CAByS desactivado exitosamente");
    }

    /**
     * Eliminar permanentemente un código CAByS
     */
    @Transactional
    public void eliminarCabysPermanente(Long id) {
        String tenant = TenantContext.getCurrentTenant();
        log.warn("Eliminando PERMANENTEMENTE CAByS con ID {} para tenant: {}", id, tenant);
        
        if (!empresaCabysRepository.existsById(id)) {
            throw new IllegalArgumentException("CAByS no encontrado con ID: " + id);
        }
        
        empresaCabysRepository.deleteById(id);
        
        log.info("CAByS eliminado permanentemente");
    }
}