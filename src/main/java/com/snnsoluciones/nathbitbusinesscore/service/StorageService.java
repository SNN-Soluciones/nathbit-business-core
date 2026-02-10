package com.snnsoluciones.nathbitbusinesscore.service;

import com.snnsoluciones.nathbitbusinesscore.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Servicio para subir archivos a S3 / DigitalOcean Spaces
 */
@Slf4j
@Service
public class StorageService {

    @Value("${storage.spaces.access-key}")
    private String accessKey;

    @Value("${storage.spaces.secret-key}")
    private String secretKey;

    @Value("${storage.spaces.bucket}")
    private String bucket;

    @Value("${storage.spaces.region}")
    private String region;

    @Value("${storage.spaces.endpoint}")
    private String endpoint;

    @Value("${storage.spaces.cdn-url}")
    private String cdnUrl;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            this.s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .build();

            log.info("✅ S3Client inicializado correctamente");

        } catch (Exception e) {
            log.error("❌ Error inicializando S3Client: {}", e.getMessage(), e);
            throw new BusinessException("Error inicializando servicio de almacenamiento", e);
        }
    }

    /**
     * Sube un archivo desde MultipartFile
     * 
     * @param file Archivo a subir
     * @param carpeta Carpeta destino (ej: "NathBit-POS/tenant_001/productos")
     * @param nombreArchivo Nombre del archivo SIN extensión (ej: "PROD-001")
     * @param isPrivate Si es privado (false = público)
     * @return URL pública del archivo
     */
    public String subirArchivo(MultipartFile file, String carpeta, String nombreArchivo, boolean isPrivate) {
        try {
            // Obtener extensión
            String extension = obtenerExtension(file.getOriginalFilename());
            
            // Construir key completa: carpeta/nombre.ext
            String key = carpeta + "/" + nombreArchivo + extension;

            log.info("📤 Subiendo archivo a S3: {}", key);

            // Determinar ACL
            ObjectCannedACL acl = isPrivate ? ObjectCannedACL.PRIVATE : ObjectCannedACL.PUBLIC_READ;

            // Subir archivo
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .acl(acl)
                .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            // Construir URL pública
            String url = cdnUrl + "/" + key;
            log.info("✅ Archivo subido: {}", url);

            return url;

        } catch (IOException e) {
            log.error("❌ Error subiendo archivo: {}", e.getMessage(), e);
            throw new BusinessException("Error al subir el archivo", e);
        }
    }

    /**
     * Sube un archivo desde bytes (para thumbnails)
     * 
     * @param bytes Contenido del archivo
     * @param key Key completa (ej: "NathBit-POS/tenant_001/productos/PROD-001_thumb.jpg")
     * @param contentType Tipo de contenido (ej: "image/jpeg")
     * @param isPrivate Si es privado
     * @return URL pública
     */
    public String subirArchivo(byte[] bytes, String key, String contentType, boolean isPrivate) {
        try {
            log.info("📤 Subiendo bytes a S3: {}", key);

            ObjectCannedACL acl = isPrivate ? ObjectCannedACL.PRIVATE : ObjectCannedACL.PUBLIC_READ;

            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .acl(acl)
                .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));

            String url = cdnUrl + "/" + key;
            log.info("✅ Bytes subidos: {}", url);

            return url;

        } catch (Exception e) {
            log.error("❌ Error subiendo bytes: {}", e.getMessage(), e);
            throw new BusinessException("Error al subir el archivo", e);
        }
    }

    /**
     * Elimina un archivo de S3
     * 
     * @param key Key del archivo (ej: "NathBit-POS/tenant_001/productos/PROD-001.jpg")
     */
    public void eliminarArchivo(String key) {
        try {
            log.info("🗑️ Eliminando archivo de S3: {}", key);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            s3Client.deleteObject(request);

            log.info("✅ Archivo eliminado: {}", key);

        } catch (Exception e) {
            log.error("❌ Error eliminando archivo: {}", e.getMessage(), e);
            throw new BusinessException("Error al eliminar el archivo", e);
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