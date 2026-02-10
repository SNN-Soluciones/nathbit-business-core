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
public class TenantConfiguracionDTO {
    
    private Long id;
    private String codigoTenant;
    
    // Facturación
    private String modoFacturacion;
    private String regimenTributario;
    private Boolean requiereHacienda;
    
    // Inventario
    private Boolean manejaInventario;
    private Boolean aplicaRecetas;
    private Boolean permiteNegativos;
    
    // Impresión
    private String modoImpresion;
    private String metodoImpresion;
    private String ipOrquestador;
    private Boolean impresionAutomatica;
    private Boolean autoImprimirFactura;
    private Boolean autoImprimirComanda;
    private Integer tiempoAutoClose;
    
    // Estado
    private Boolean activa;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}