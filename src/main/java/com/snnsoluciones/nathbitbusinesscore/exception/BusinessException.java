package com.snnsoluciones.nathbitbusinesscore.exception;

/**
 * Excepción de negocio para el sistema
 * Se lanza cuando se violan reglas de negocio o validaciones
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}