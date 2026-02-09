package com.snnsoluciones.nathbitbusinesscore.interceptor;

import com.snnsoluciones.nathbitbusinesscore.context.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor que:
 * 1. Lee el header X-Device-Token
 * 2. Decodifica el JWT para obtener tenantCodigo
 * 3. Configura el schema del tenant: SET search_path TO tenant_X, public
 */
@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 🔥 1. Obtener X-Device-Token del header
        String deviceToken = request.getHeader("X-Device-Token");

        if (deviceToken == null || deviceToken.isEmpty() || "UNKNOWN".equals(deviceToken)) {
            log.warn("❌ No hay X-Device-Token en la request - rechazando");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        try {
            // 🔥 2. Decodificar JWT para obtener tenantCodigo
            Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))  // 👈 Nuevo
                .build()                                                // 👈 Nuevo
                .parseSignedClaims(deviceToken)                        // 👈 Nuevo
                .getPayload();                                         // 👈 Nuevo


            String tenantCodigo = claims.get("tenantCodigo", String.class);

            if (tenantCodigo == null || tenantCodigo.isEmpty()) {
                log.error("❌ JWT no contiene tenantCodigo");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            // 🔥 3. Construir nombre del schema (tenant_CODIGO)
            String tenantSchema = "tenant_" + tenantCodigo;

            log.info("🔐 Tenant detectado: {} (schema: {})", tenantCodigo, tenantSchema);

            // 🔥 4. Setear en el contexto
            TenantContext.setCurrentTenant(tenantSchema);

            // 🔥 5. Configurar search_path en PostgreSQL
            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                try (var stmt = connection.createStatement()) {
                    String sql = String.format("SET search_path TO %s, public", tenantSchema);
                    stmt.execute(sql);
                    log.debug("✅ Search path configurado: {}", sql);
                }
            });

        } catch (Exception e) {
            log.error("❌ Error procesando X-Device-Token:", e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
        Object handler, Exception ex) {
        // Limpiar el contexto después de la request
        TenantContext.clear();
        log.debug("🧹 Contexto de tenant limpiado");
    }
}