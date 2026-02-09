package com.snnsoluciones.nathbitbusinesscore.specification;

import com.snnsoluciones.nathbitbusinesscore.model.dto.productos.ProductoSearchDTO;
import com.snnsoluciones.nathbitbusinesscore.model.entity.CategoriaProducto;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.model.entity.ProductoImpuesto;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification para búsqueda dinámica de productos.
 * Permite combinar múltiples filtros de forma flexible.
 * 
 * CARACTERÍSTICAS:
 * - Búsqueda por código (interno O barras)
 * - Búsqueda por nombre (parcial, case-insensitive)
 * - Búsqueda por precio CONSIDERANDO IMPUESTOS
 * - Filtros por tipo, zona, categoría, impuesto
 * - Filtros booleanos (requiereReceta, requiereInventario)
 */
@Slf4j
public class ProductoSpecification {

    /**
     * Crea especificación combinada de todos los filtros aplicables.
     * 
     * @param filtros DTO con criterios de búsqueda
     * @return Specification para usar con JpaSpecificationExecutor
     */
    public static Specification<Producto> crearBusqueda(ProductoSearchDTO filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ==================== 1. BÚSQUEDA POR CÓDIGO ====================
            // Busca en codigo_interno O codigo_barras (LIKE parcial)
            if (filtros.getCodigo() != null && !filtros.getCodigo().trim().isEmpty()) {
                String codigoLike = "%" + filtros.getCodigo().trim().toLowerCase() + "%";
                
                Predicate codigoInterno = cb.like(
                    cb.lower(root.get("codigoInterno")), 
                    codigoLike
                );
                
                Predicate codigoBarras = cb.like(
                    cb.lower(root.get("codigoBarras")), 
                    codigoLike
                );
                
                predicates.add(cb.or(codigoInterno, codigoBarras));
                
                log.debug("Filtro código aplicado: {}", filtros.getCodigo());
            }

            // ==================== 2. BÚSQUEDA POR NOMBRE ====================
            // LIKE parcial, case-insensitive
            if (filtros.getNombre() != null && !filtros.getNombre().trim().isEmpty()) {
                String nombreLike = "%" + filtros.getNombre().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("nombre")), nombreLike));
                
                log.debug("Filtro nombre aplicado: {}", filtros.getNombre());
            }

            // ==================== 3. BÚSQUEDA POR PRECIO (CON IMPUESTOS) ====================
            // COMPLEJIDAD: Calcula precio con impuesto y busca con margen ±1
            // Ejemplo: Si busco "113", encuentra productos con precio_venta=100 + IVA 13%
            // ==================== 3. BÚSQUEDA POR PRECIO (CON IMPUESTOS) ====================
            if (filtros.getPrecio() != null) {
                // JOIN con tabla de impuestos
                Join<Producto, ProductoImpuesto> impuestosJoin = root.join("impuestos", JoinType.LEFT);

                // Calcular precio con impuesto:
                // precioConImpuesto = precioVenta * (1 + porcentaje/100)

                // Paso 1: porcentaje / 100 (ejemplo: 13 / 100 = 0.13)
                Expression<Number> porcentajeDecimal = cb.quot(
                    impuestosJoin.get("porcentaje"),
                    100
                );

                // Paso 2: 1 + 0.13 = 1.13
                Expression<Number> factorImpuesto = cb.sum(
                    1,
                    porcentajeDecimal
                );

                // Paso 3: precioVenta * 1.13
                Expression<Number> precioConImpuesto = cb.prod(
                    root.get("precioVenta"),
                    factorImpuesto
                );

                // Buscar con margen de ±1 para compensar redondeos
                BigDecimal margen = BigDecimal.ONE;
                BigDecimal precioMinimo = filtros.getPrecio().subtract(margen);
                BigDecimal precioMaximo = filtros.getPrecio().add(margen);

                // ✅ USAR ge() y le() en lugar de between()
                Predicate mayorIgual = cb.ge(precioConImpuesto, precioMinimo);
                Predicate menorIgual = cb.le(precioConImpuesto, precioMaximo);

                predicates.add(cb.and(mayorIgual, menorIgual));

                log.debug("Filtro precio aplicado: {} (rango: {} - {})",
                    filtros.getPrecio(), precioMinimo, precioMaximo);
            }

            // ==================== 4. BÚSQUEDA POR TIPO ====================
            // Tipo exacto (VENTA, MATERIA_PRIMA, MIXTO, COMBO, COMPUESTO)
            if (filtros.getTipo() != null && !filtros.getTipo().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("tipo"), filtros.getTipo()));
                
                log.debug("Filtro tipo aplicado: {}", filtros.getTipo());
            }

            // ==================== 5. BÚSQUEDA POR ZONA ====================
            // Zona exacta (NINGUNA, COCINA, BAR, etc)
            if (filtros.getZona() != null && !filtros.getZona().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("zonaPreparacion"), filtros.getZona()));
                
                log.debug("Filtro zona aplicado: {}", filtros.getZona());
            }

            // ==================== 6. BÚSQUEDA POR IMPUESTO ====================
            // Busca productos con un código de tarifa IVA específico
            // Ejemplo: TARIFA_GENERAL_13, TARIFA_EXENTA
            if (filtros.getCodigoTarifaImpuesto() != null && !filtros.getCodigoTarifaImpuesto().trim().isEmpty()) {
                Join<Producto, ProductoImpuesto> impuestosJoin = root.join("impuestos", JoinType.INNER);
                
                predicates.add(cb.equal(
                    impuestosJoin.get("codigoTarifaIva"), 
                    filtros.getCodigoTarifaImpuesto()
                ));
                
                predicates.add(cb.equal(impuestosJoin.get("activo"), true));
                
                log.debug("Filtro impuesto aplicado: {}", filtros.getCodigoTarifaImpuesto());
            }

            // ==================== 7. BÚSQUEDA POR REQUIERE RECETA ====================
            if (filtros.getRequiereReceta() != null) {
                predicates.add(cb.equal(root.get("requiereReceta"), filtros.getRequiereReceta()));
                
                log.debug("Filtro requiereReceta aplicado: {}", filtros.getRequiereReceta());
            }

            // ==================== 8. BÚSQUEDA POR REQUIERE INVENTARIO ====================
            if (filtros.getRequiereInventario() != null) {
                predicates.add(cb.equal(root.get("requiereInventario"), filtros.getRequiereInventario()));
                
                log.debug("Filtro requiereInventario aplicado: {}", filtros.getRequiereInventario());
            }

            // ==================== 9. BÚSQUEDA POR CATEGORÍA ====================
            // JOIN con tabla producto_categoria
            if (filtros.getCategoriaId() != null) {
                Join<Producto, CategoriaProducto> categoriasJoin = root.join("categorias", JoinType.INNER);
                
                predicates.add(cb.equal(categoriasJoin.get("id"), filtros.getCategoriaId()));
                
                log.debug("Filtro categoría aplicado: {}", filtros.getCategoriaId());
            }

            // ==================== 10. FILTRO POR ACTIVO ====================
            // Siempre aplicado (default: true)
            if (filtros.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), filtros.getActivo()));
                
                log.debug("Filtro activo aplicado: {}", filtros.getActivo());
            }

            // ==================== ELIMINAR DUPLICADOS ====================
            // CRÍTICO: Si hay JOINs, pueden aparecer productos duplicados
            // distinct(true) los elimina
            query.distinct(true);

            // Combinar todos los predicates con AND
            log.debug("Total de filtros aplicados: {}", predicates.size());
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification simple para buscar productos activos.
     * Útil cuando no hay filtros adicionales.
     */
    public static Specification<Producto> soloActivos() {
        return (root, query, cb) -> cb.equal(root.get("activo"), true);
    }

    /**
     * Specification para buscar por tipo.
     */
    public static Specification<Producto> porTipo(String tipo) {
        return (root, query, cb) -> {
            if (tipo == null || tipo.trim().isEmpty()) {
                return cb.conjunction(); // No aplica filtro
            }
            return cb.equal(root.get("tipo"), tipo);
        };
    }

    /**
     * Specification para buscar por zona.
     */
    public static Specification<Producto> porZona(String zona) {
        return (root, query, cb) -> {
            if (zona == null || zona.trim().isEmpty()) {
                return cb.conjunction(); // No aplica filtro
            }
            return cb.equal(root.get("zonaPreparacion"), zona);
        };
    }
}