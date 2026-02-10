package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Servicio para procesar imágenes (redimensionar, generar thumbnails)
 */
@Slf4j
@Service
public class ImageProcessingService {

    /**
     * Genera un thumbnail de la imagen
     * 
     * @param file Imagen original
     * @param width Ancho del thumbnail
     * @param height Alto del thumbnail
     * @return Bytes del thumbnail en formato JPEG
     */
    public byte[] generarThumbnail(MultipartFile file, int width, int height) {
        try {
            log.debug("📐 Generando thumbnail {}x{} para: {}", width, height, file.getOriginalFilename());

            // Leer imagen original
            BufferedImage original = ImageIO.read(file.getInputStream());

            if (original == null) {
                throw new BusinessException("No se pudo leer la imagen");
            }

            // Calcular dimensiones proporcionales
            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();
            double ratio = (double) originalWidth / originalHeight;

            int newWidth = width;
            int newHeight = height;

            if (ratio > 1) {
                // Imagen horizontal
                newHeight = (int) (width / ratio);
            } else {
                // Imagen vertical
                newWidth = (int) (height * ratio);
            }

            // Redimensionar
            BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnail.createGraphics();
            
            // Configurar calidad
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.drawImage(original, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // Convertir a bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            log.debug("✅ Thumbnail generado: {} bytes", bytes.length);
            return bytes;

        } catch (IOException e) {
            log.error("❌ Error generando thumbnail: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar la imagen", e);
        }
    }
}