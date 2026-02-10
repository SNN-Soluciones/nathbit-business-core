package com.snnsoluciones.nathbitbusinesscore.model.dto.productos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas paginadas de productos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoPage {

    private List<ProductoListDTO> productos;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}