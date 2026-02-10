package com.snnsoluciones.nathbitbusinesscore.service.handler;

import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import com.snnsoluciones.nathbitbusinesscore.model.entity.Producto;
import com.snnsoluciones.nathbitbusinesscore.repository.ProductoRepository;
import com.snnsoluciones.nathbitbusinesscore.service.ImageProcessingService;
import com.snnsoluciones.nathbitbusinesscore.service.StorageService;
import com.snnsoluciones.nathbitbusinesscore.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

/**
 * Handler para gestión de imágenes de productos
 * Sube a S3/DigitalOcean Spaces con estructura por tenant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoImagenHandler {

    private final StorageService storageService;
    private final ProductoRepository productoRepository;
    private final TenantService tenantService;
    private final ImageProcessingService imageProcessingService;

    @Value("${app.storage.max-file-size:5242880}") // 5MB
    private long maxFileSize;

    @Value("${storage.spaces.base-folder:NathBit-POS}")
    private String baseFolder;

    private static final List<String> EXTENSIONES_PERMITIDAS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".webp"
    );

    private static final List<String> CONTENT_TYPES_PERMITIDOS = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    /**
     * Sube imagen + thumbnail para un producto
     */
    public void subirImagen(Producto producto, MultipartFile imagen) {
        log.info("📸 Subiendo imagen para producto {}", producto.getCodigoInterno());

        validarImagen(imagen);

        try {
            // 1️⃣ Construir carpeta: NathBit-POS/{nombre_comercial}/productos
            String carpeta = construirCarpetaProductos();
            String nombreArchivo = producto.getCodigoInterno();
            String extension = obtenerExtension(imagen.getOriginalFilename());

            log.debug("📁 Carpeta: {}", carpeta);
            log.debug("📄 Archivo: {}{}", nombreArchivo, extension);

            // 2️⃣ Generar thumbnail
            byte[] thumbnailBytes = null;
            try {
                thumbnailBytes = imageProcessingService.generarThumbnail(imagen, 150, 150);
                log.debug("✅ Thumbnail generado ({} bytes)", thumbnailBytes.length);
            } catch (Exception e) {
                log.warn("⚠️ Error generando thumbnail: {}", e.getMessage());
            }

            // 3️⃣ Subir imagen original
            String urlOriginal = storageService.subirArchivo(
                imagen,
                carpeta,
                nombreArchivo,
                false // público
            );
            String keyOriginal = carpeta + "/" + nombreArchivo + extension;

            // 4️⃣ Subir thumbnail
            String urlThumbnail = null;
            String keyThumbnail = null;

            if (thumbnailBytes != null && thumbnailBytes.length > 0) {
                try {
                    String nombreThumbnail = nombreArchivo + "_thumb";
                    keyThumbnail = carpeta + "/" + nombreThumbnail + ".jpg";

                    urlThumbnail = storageService.subirArchivo(
                        thumbnailBytes,
                        keyThumbnail,
                        "image/jpeg",
                        false
                    );
                    log.info("✅ Thumbnail subido: {}", keyThumbnail);
                } catch (Exception e) {
                    log.error("❌ Error subiendo thumbnail: {}", e.getMessage());
                }
            }

            // 5️⃣ Guardar URLs en producto
            producto.setImagenUrl(urlOriginal);
            producto.setImagenKey(keyOriginal);
            producto.setThumbnailUrl(urlThumbnail);
            producto.setThumbnailKey(keyThumbnail);
            productoRepository.save(producto);

            log.info("✅ Imagen subida: {}", urlOriginal);

        } catch (Exception e) {
            log.error("❌ Error subiendo imagen: {}", e.getMessage(), e);
            throw new BusinessException("Error al subir la imagen: " + e.getMessage());
        }
    }

    /**
     * Actualiza imagen (elimina anterior y sube nueva)
     */
    public void actualizarImagen(Producto producto, MultipartFile nuevaImagen) {
        log.info("🔄 Actualizando imagen producto {}", producto.getId());
        
        validarImagen(nuevaImagen);
        eliminarImagenesAnteriores(producto);
        subirImagen(producto, nuevaImagen);
    }

    /**
     * Elimina imagen del producto
     */
    public void eliminarImagen(Producto producto) {
        log.info("🗑️ Eliminando imagen producto {}", producto.getId());

        if (producto.getImagenKey() == null || producto.getImagenKey().isEmpty()) {
            log.warn("Producto sin imagen asociada");
            return;
        }

        eliminarImagenesAnteriores(producto);

        producto.setImagenUrl(null);
        producto.setImagenKey(null);
        producto.setThumbnailUrl(null);
        producto.setThumbnailKey(null);
        productoRepository.save(producto);

        log.info("✅ Imagen eliminada");
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Construye la carpeta para imágenes usando el nombre comercial del tenant
     * Estructura: NathBit-POS/{nombre_comercial}/productos
     * 
     * @return Carpeta sanitizada (ej: "NathBit-POS/restaurante-la-esquina/productos")
     */
    private String construirCarpetaProductos() {
        String nombreComercial = tenantService.obtenerNombreComercial();
        String nombreSanitizado = sanitizarNombre(nombreComercial);
        
        return String.format("%s/%s/productos", baseFolder, nombreSanitizado);
    }

    /**
     * Sanitiza el nombre comercial para usarlo en rutas S3
     * - Quita acentos y diacríticos
     * - Reemplaza espacios con guiones
     * - Solo permite letras, números y guiones
     * - Convierte a minúsculas
     * 
     * @param nombre Nombre original
     * @return Nombre sanitizado (ej: "restaurante-la-esquina")
     */
    private String sanitizarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "default";
        }

        // 1. Normalizar y quitar diacríticos (acentos, ñ, etc.)
        String normalizado = Normalizer.normalize(nombre, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("\\p{M}", ""); // Quitar marcas diacríticas

        // 2. Convertir a minúsculas y trimear
        String sanitizado = normalizado.toLowerCase().trim();

        // 3. Reemplazar espacios y caracteres especiales con guiones
        sanitizado = sanitizado.replaceAll("\\s+", "-");           // Espacios → guiones
        sanitizado = sanitizado.replaceAll("[^a-z0-9-]", "");      // Solo letras, números y guiones
        sanitizado = sanitizado.replaceAll("-+", "-");             // Múltiples guiones → uno solo
        sanitizado = sanitizado.replaceAll("^-|-$", "");           // Quitar guiones al inicio/final

        // 4. Si queda vacío, usar "default"
        if (sanitizado.isEmpty()) {
            return "default";
        }

        return sanitizado;
    }

    /**
     * Valida que la imagen cumpla con los requisitos
     */
    private void validarImagen(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new BusinessException("La imagen es requerida");
        }

        // Validar tamaño
        if (imagen.getSize() > maxFileSize) {
            throw new BusinessException(
                String.format("La imagen excede el tamaño máximo permitido de %.2f MB", 
                    maxFileSize / 1024.0 / 1024.0)
            );
        }

        // Validar content type
        String contentType = imagen.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new BusinessException(
                "Tipo de archivo no permitido. Solo se permiten: JPG, PNG, WEBP"
            );
        }

        // Validar extensión
        String extension = obtenerExtension(imagen.getOriginalFilename());
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new BusinessException(
                "Extensión no permitida. Solo se permiten: .jpg, .jpeg, .png, .webp"
            );
        }
    }

    /**
     * Elimina imágenes anteriores del producto (original + thumbnail)
     */
    private void eliminarImagenesAnteriores(Producto producto) {
        // Eliminar imagen original
        if (producto.getImagenKey() != null && !producto.getImagenKey().isEmpty()) {
            try {
                storageService.eliminarArchivo(producto.getImagenKey());
                log.debug("✅ Imagen original eliminada: {}", producto.getImagenKey());
            } catch (Exception e) {
                log.warn("⚠️ Error eliminando imagen original: {}", e.getMessage());
            }
        }

        // Eliminar thumbnail
        if (producto.getThumbnailKey() != null && !producto.getThumbnailKey().isEmpty()) {
            try {
                storageService.eliminarArchivo(producto.getThumbnailKey());
                log.debug("✅ Thumbnail eliminado: {}", producto.getThumbnailKey());
            } catch (Exception e) {
                log.warn("⚠️ Error eliminando thumbnail: {}", e.getMessage());
            }
        }
    }

    /**
     * Obtiene la extensión del archivo (incluyendo el punto)
     */
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".jpg";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).toLowerCase();
    }
}