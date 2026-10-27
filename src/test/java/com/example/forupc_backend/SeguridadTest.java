
package com.example.forupc_backend;

import com.example.forupc_backend.modelo.Rol;
import com.example.forupc_backend.modelo.Usuario;
import com.example.forupc_backend.repository.UsuarioRepository;
import com.example.forupc_backend.seguridad.JwtService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTest {

    // ==========================================
    // CONFIGURACION DE PRUEBAS
    // ==========================================

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // ==========================================
    // CREAR USUARIO DE PRUEBA
    // ==========================================

    private Usuario crearUsuario(String email, Rol rol) {

        return new Usuario(
                email,
                "Usuario",
                "Prueba",
                passwordEncoder.encode("Clave1234!"),
                rol
        );
    }

    // ==========================================
    // SIMULAR USUARIO EXISTENTE
    // ==========================================

    private void prepararUsuario(String email, Rol rol) {

        Usuario usuario = crearUsuario(email, rol);

        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(usuario));
    }

    // ==========================================
    // LOGIN Y OBTENCION DEL TOKEN JWT
    // ==========================================

    private String login(String email, String password)
            throws Exception {

        String respuesta = mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        mapper.writeValueAsString(
                                                Map.of(
                                                        "email", email,
                                                        "password", password
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = mapper.readTree(respuesta);

        return json.get("token").asText();
    }

    // ==========================================
    // PRUEBA 1: LOGIN CORRECTO
    // ==========================================

    @Test
    void loginCorrectoDevuelveToken() throws Exception {

        prepararUsuario(
                "estudiante@test.com",
                Rol.ESTUDIANTE
        );

        String token = login(
                "estudiante@test.com",
                "Clave1234!"
        );

        assertFalse(token.isBlank());
    }

    // ==========================================
    // PRUEBA 2: LOGIN INCORRECTO
    // ==========================================

    @Test
    void loginIncorrectoDevuelve401() throws Exception {

        prepararUsuario(
                "estudiante@test.com",
                Rol.ESTUDIANTE
        );

        mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        mapper.writeValueAsString(
                                                Map.of(
                                                        "email", "estudiante@test.com",
                                                        "password", "incorrecta"
                                                )
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // PRUEBA 3: ACCESO SIN TOKEN
    // ==========================================

    @Test
    void accesoSinTokenDevuelve401() throws Exception {

        mvc.perform(
                        get("/usuarios/me")
                )
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // PRUEBA 4: ESTUDIANTE SIN PERMISOS
    // ==========================================

    @Test
    void estudianteNoPuedeListarUsuarios() throws Exception {

        String email = "estudiante@test.com";

        String token = jwtService.generarToken(
                email,
                Rol.ESTUDIANTE.name()
        );

        mvc.perform(
                        get("/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // PRUEBA 5: ADMINISTRADOR AUTORIZADO
    // ==========================================

    @Test
    void administradorPuedeListarUsuarios() throws Exception {

        String email = "admin@test.com";

        String token = jwtService.generarToken(
                email,
                Rol.ADMINISTRADOR.name()
        );

        when(usuarioRepository.findAll())
                .thenReturn(List.of());

        mvc.perform(
                        get("/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }
}
