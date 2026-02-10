package com.snnsoluciones.nathbitbusinesscore.model.dto.tenant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantConfiguracionDTO {
    
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
    
    @Min(value = 0, message = "El tiempo debe ser mayor o igual a 0")
    @Max(value = 60, message = "El tiempo no puede exceder 60 segundos")
    private Integer tiempoAutoClose;
    
    private Boolean activa;
}