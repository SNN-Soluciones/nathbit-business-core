package com.snnsoluciones.nathbitbusinesscore.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad para el microservicio business-core.
 * 
 * - Deshabilita CSRF (API stateless)
 * - Configura CORS para permitir requests desde el frontend
 * - Permite todas las peticiones (por ahora)
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("⚙️ Configurando Spring Security...");

        http
            // Deshabilitar CSRF (no necesario para API stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Sesiones stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Permitir todas las peticiones (por ahora)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        log.info("✅ Spring Security configurado");
        return http.build();
    }

    /**
     * Configuración de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("⚙️ Configurando CORS...");

        CorsConfiguration configuration = new CorsConfiguration();

        // 🔥 CAMBIO: Usar allowedOriginPatterns en lugar de allowedOrigins
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",       // Angular dev (cualquier puerto)
            "https://localhost",         // Capacitor Android/iOS
            "capacitor://localhost",     // Capacitor alternativo
            "ionic://localhost",         // Ionic alternativo
            "https://app.nathbit.com",  // Producción
            "*"                          // 🔥 Permite cualquier origen en dev
        ));

        // Permitir todos los métodos HTTP
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Permitir todos los headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Device-Token",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // Exponer headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Device-Token"
        ));

        // Permitir credenciales
        configuration.setAllowCredentials(true);

        // Max age para preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("✅ CORS configurado");

        return source;
    }
}