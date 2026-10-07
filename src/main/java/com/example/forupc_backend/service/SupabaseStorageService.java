package com.example.forupc_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final RestClient restClient;

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-key}") String serviceKey,
            @Value("${supabase.storage.bucket}") String bucket
    ) {

        this.supabaseUrl = supabaseUrl;
        this.serviceKey = serviceKey;
        this.bucket = bucket;

        this.restClient = RestClient.builder().build();
    }

    public String subirImagen(MultipartFile archivo) {

        validarImagen(archivo);

        return subirArchivo(
                archivo,
                "imagenes"
        );
    }

    public ArchivoSubido subirDocumento(MultipartFile archivo) {

        validarDocumento(archivo);

        String url = subirArchivo(
                archivo,
                "documentos"
        );

        return new ArchivoSubido(
                url,
                archivo.getOriginalFilename()
        );
    }

    private String subirArchivo(
            MultipartFile archivo,
            String carpeta
    ) {

        try {

            String extension = obtenerExtension(
                    archivo.getOriginalFilename()
            );

            String nombre = UUID.randomUUID()
                    + extension;

            String ruta = carpeta + "/" + nombre;

            String url = supabaseUrl
                    + "/storage/v1/object/"
                    + bucket
                    + "/"
                    + ruta;

            MediaType contentType =
                    archivo.getContentType() != null
                            ? MediaType.parseMediaType(
                            archivo.getContentType()
                    )
                            : MediaType.APPLICATION_OCTET_STREAM;

            restClient.post()
                    .uri(url)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + serviceKey
                    )
                    .header(
                            "apikey",
                            serviceKey
                    )
                    .header(
                            "x-upsert",
                            "false"
                    )
                    .contentType(contentType)
                    .body(archivo.getBytes())
                    .retrieve()
                    .toBodilessEntity();

            return supabaseUrl
                    + "/storage/v1/object/public/"
                    + bucket
                    + "/"
                    + ruta;

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo subir el archivo a Supabase Storage.",
                    e
            );
        }
    }

    private void validarImagen(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "La imagen está vacía."
            );
        }

        String tipo = archivo.getContentType();

        if (tipo == null ||
                !tipo.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "El archivo enviado no es una imagen válida."
            );
        }

        if (archivo.getSize() > 5 * 1024 * 1024) {

            throw new IllegalArgumentException(
                    "La imagen no puede superar los 5 MB."
            );
        }
    }

    private void validarDocumento(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El documento está vacío."
            );
        }

        String nombre =
                archivo.getOriginalFilename();

        String extension =
                obtenerExtension(nombre)
                        .toLowerCase();

        if (!extension.equals(".pdf") &&
                !extension.equals(".doc") &&
                !extension.equals(".docx")) {

            throw new IllegalArgumentException(
                    "Solo se permiten archivos PDF, DOC o DOCX."
            );
        }

        if (archivo.getSize() > 10 * 1024 * 1024) {

            throw new IllegalArgumentException(
                    "El documento no puede superar los 10 MB."
            );
        }
    }

    private String obtenerExtension(String nombre) {

        if (nombre == null ||
                !nombre.contains(".")) {

            return "";
        }

        return nombre.substring(
                nombre.lastIndexOf(".")
        );
    }

    public record ArchivoSubido(
            String url,
            String nombre
    ) {
    }
}