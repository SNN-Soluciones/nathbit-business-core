package com.snnsoluciones.nathbitbusinesscore.model.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantInfoDTO {
    
    private Long id;
    private String codigoTenant;
    private String nombre;
    private String nombreComercial;
    private String telefono;
    private String email;
    private String provinciaId;
    private String cantonId;
    private String distritoId;
    private String barrioId;
    private String otrasSenas;
    private String logoUrl;
    private String logoKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}