package com.example.forupc_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "For-UPC - API REST",
                version = "1.0.0",
                description = """
            API REST del Portal Informativo Academico
            de la Universidad Provincial de Cordoba,
            Sede Laboulaye.

            Funcionalidades principales:
            - Autenticacion de usuarios mediante JWT.
            - Gestion de estudiantes y administradores.
            - Gestion de carreras y anios academicos.
            - Inscripciones a carreras.
            - Publicacion y consulta de anuncios.
            - Almacenamiento de archivos mediante Supabase.

            Tecnologias:
            Java 21, Spring Boot 4, PostgreSQL,
            Supabase y Spring Security.
            """
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingrese el token JWT obtenido en /auth/login"
)
public class OpenApiConfig {
}
