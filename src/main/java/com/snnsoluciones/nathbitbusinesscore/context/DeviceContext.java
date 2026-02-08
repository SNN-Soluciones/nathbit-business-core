package com.snnsoluciones.nathbitbusinesscore.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Contexto para manejar el dispositivo actual en cada thread.
 * Información extraída del device-token.
 */
@Slf4j
@Component
public class DeviceContext {

  private static final ThreadLocal<DeviceInfo> currentDevice = new ThreadLocal<>();

  public static void setCurrentDevice(String deviceId, String deviceName, String tenantId) {
    DeviceInfo deviceInfo = new DeviceInfo(deviceId, deviceName, tenantId);
    log.debug("Setting device context: {}", deviceInfo);
    currentDevice.set(deviceInfo);
  }

  public static DeviceInfo getCurrentDevice() {
    return currentDevice.get();
  }

  public static String getCurrentDeviceId() {
    DeviceInfo device = currentDevice.get();
    return device != null ? device.getDeviceId() : null;
  }

  public static String getCurrentDeviceName() {
    DeviceInfo device = currentDevice.get();
    return device != null ? device.getDeviceName() : null;
  }

  public static String getDeviceTenantId() {
    DeviceInfo device = currentDevice.get();
    return device != null ? device.getTenantId() : null;
  }

  public static void clear() {
    log.debug("Clearing device context");
    currentDevice.remove();
  }

  public static boolean hasDevice() {
    return currentDevice.get() != null;
  }

  public static DeviceInfo getCurrentDeviceOrThrow() {
    DeviceInfo device = getCurrentDevice();
    if (device == null) {
      throw new IllegalStateException("No device configured in context");
    }
    return device;
  }

  /**
   * Clase interna para almacenar información del dispositivo
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeviceInfo {
    private String deviceId;
    private String deviceName;
    private String tenantId;
  }
}