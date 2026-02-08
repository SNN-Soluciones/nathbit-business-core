package com.snnsoluciones.nathbitbusinesscore.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Contexto para manejar el tenant actual en cada thread.
 * Almacena el schema del tenant (ej: tenant_viaje_al_sabor)
 */
@Slf4j
@Component
public class TenantContext {

  private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

  public static void setCurrentTenant(String tenant) {
    log.debug("Setting tenant to: {}", tenant);
    currentTenant.set(tenant);
  }

  public static String getCurrentTenant() {
    String tenant = currentTenant.get();
    log.debug("Getting current tenant: {}", tenant);
    return tenant;
  }

  public static void clear() {
    log.debug("Clearing tenant context");
    currentTenant.remove();
  }

  /**
   * Verifica si hay un tenant activo en el contexto
   */
  public static boolean hasTenant() {
    return currentTenant.get() != null;
  }

  /**
   * Obtiene el tenant o lanza excepción si no está configurado
   */
  public static String getCurrentTenantOrThrow() {
    String tenant = getCurrentTenant();
    if (tenant == null) {
      throw new IllegalStateException("No tenant configured in context");
    }
    return tenant;
  }
}