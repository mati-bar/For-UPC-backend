package com.example.forupc_backend.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =====================================
    // CONFIGURACION CORS
    // =====================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept"
        ));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // =====================================
    // SEGURIDAD HTTP Y JWT
    // =====================================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Registro, login y recuperacion
                        .requestMatchers("/auth/**").permitAll()

                        // Documentacion API
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Consultas publicas
                        .requestMatchers(HttpMethod.GET,
                                "/publicaciones", "/publicaciones/**",
                                "/carreras", "/carreras/**",
                                "/anios", "/anios/**"
                        ).permitAll()

                        // Administrar publicaciones
                        .requestMatchers(HttpMethod.PUT, "/publicaciones/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/publicaciones", "/publicaciones/")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/publicaciones/**")
                        .hasRole("ADMINISTRADOR")

                        // Administrar carreras
                        .requestMatchers(HttpMethod.POST, "/carreras", "/carreras/")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/carreras/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/carreras/**")
                        .hasRole("ADMINISTRADOR")

                        // Administracion de estudiantes
                        .requestMatchers(HttpMethod.GET, "/usuarios", "/usuarios/")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/{id}")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/{id}")
                        .hasRole("ADMINISTRADOR")

                        // Errores
                        .requestMatchers("/error").permitAll()

                        // Perfil del usuario autenticado
                        .requestMatchers("/usuarios/me", "/usuarios/me/inscripciones")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
