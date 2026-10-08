
package com.example.forupc_backend.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // AUTENTICACION
                        .requestMatchers("/auth/**").permitAll()

                        // DOCUMENTACION API
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // CONSULTAS PUBLICAS
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/publicaciones",
                                "/publicaciones/**",
                                "/carreras/**",
                                "/anios/**"
                        ).permitAll()

                        // CREAR PUBLICACIONES:
                        // SOLO ADMINISTRADORES
                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/publicaciones",
                                "/publicaciones/"
                        ).hasRole("ADMINISTRADOR")

                        // ELIMINAR PUBLICACIONES:
                        // SOLO ADMINISTRADORES
                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/publicaciones/**"
                        ).hasRole("ADMINISTRADOR")

                        // ERRORES
                        .requestMatchers("/error").permitAll()

                        // RESTO DE RUTAS
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
