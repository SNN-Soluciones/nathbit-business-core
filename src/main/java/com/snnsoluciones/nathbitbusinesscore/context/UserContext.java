package com.snnsoluciones.nathbitbusinesscore.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Contexto para manejar el usuario actual en cada thread.
 * Información extraída del Bearer token.
 */
@Slf4j
@Component
public class UserContext {

  private static final ThreadLocal<UserInfo> currentUser = new ThreadLocal<>();

  public static void setCurrentUser(Long userId, String username, String role) {
    UserInfo userInfo = new UserInfo(userId, username, role);
    log.debug("Setting user context: {}", userInfo);
    currentUser.set(userInfo);
  }

  public static UserInfo getCurrentUser() {
    return currentUser.get();
  }

  public static Long getCurrentUserId() {
    UserInfo user = currentUser.get();
    return user != null ? user.getUserId() : null;
  }

  public static String getCurrentUsername() {
    UserInfo user = currentUser.get();
    return user != null ? user.getUsername() : null;
  }

  public static String getCurrentUserRole() {
    UserInfo user = currentUser.get();
    return user != null ? user.getRole() : null;
  }

  public static void clear() {
    log.debug("Clearing user context");
    currentUser.remove();
  }

  public static boolean hasUser() {
    return currentUser.get() != null;
  }

  public static UserInfo getCurrentUserOrThrow() {
    UserInfo user = getCurrentUser();
    if (user == null) {
      throw new IllegalStateException("No user configured in context");
    }
    return user;
  }

  /**
   * Clase interna para almacenar información del usuario
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserInfo {
    private Long userId;
    private String username;
    private String role;
  }
}