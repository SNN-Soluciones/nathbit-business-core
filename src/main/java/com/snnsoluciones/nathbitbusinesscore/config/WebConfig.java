package com.snnsoluciones.nathbitbusinesscore.config;

import com.snnsoluciones.nathbitbusinesscore.interceptor.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración Web para registrar interceptores.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar el TenantInterceptor para todas las rutas
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**");  // Solo rutas del negocio
    }
}