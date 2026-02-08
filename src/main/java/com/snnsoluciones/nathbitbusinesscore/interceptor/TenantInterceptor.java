package com.snnsoluciones.nathbitbusinesscore.interceptor;

import com.snnsoluciones.nathbitbusinesscore.context.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor que configura el schema del tenant antes de cada request.
 * Ejecuta: SET search_path TO tenant_X, public
 */
@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = TenantContext.getCurrentTenant();
        
        if (tenantId != null) {
            log.debug("Configurando search_path para tenant: {}", tenantId);
            
            try {
                // Obtener la sesión de Hibernate
                Session session = entityManager.unwrap(Session.class);
                
                // Ejecutar SET search_path
                session.doWork(connection -> {
                    try (var stmt = connection.createStatement()) {
                        String sql = String.format("SET search_path TO %s, public", tenantId);
                        stmt.execute(sql);
                        log.debug("Search path configurado: {}", sql);
                    }
                });
                
            } catch (Exception e) {
                log.error("Error configurando search_path para tenant: {}", tenantId, e);
                return false;
            }
        } else {
            log.warn("No hay tenant configurado en el contexto");
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // Limpiar el contexto después de la request
        TenantContext.clear();
        log.debug("Contexto de tenant limpiado");
    }
}