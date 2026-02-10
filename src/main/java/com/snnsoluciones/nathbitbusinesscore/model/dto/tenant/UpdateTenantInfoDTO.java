package com.snnsoluciones.nathbitbusinesscore.model.dto.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantInfoDTO {
    
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;
    
    @Size(max = 100, message = "El nombre comercial no puede exceder 100 caracteres")
    private String nombreComercial;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;
    
    @Email(message = "Email inválido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    private String provinciaId;
    private String cantonId;
    private String distritoId;
    private String barrioId;
    
    @Size(max = 500, message = "Otras señas no puede exceder 500 caracteres")
    private String otrasSenas;
}