package co.frameworks.tienda.controller;

import co.frameworks.tienda.model.Rol;
import co.frameworks.tienda.model.Usuario;
import co.frameworks.tienda.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas del Controlador REST de Autenticación")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/auth/login con credenciales válidas debe retornar HTTP 200 y rol")
    void testLoginExitoso() throws Exception {
        Usuario user = new Usuario("cliente", "$2a$10$encoded", Rol.CLIENTE);
        user.setId(10L);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("cliente", "cli123"));
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(user));

        String json = "{\"username\":\"cliente\",\"password\":\"cli123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("cliente"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("POST /api/auth/login con credenciales incorrectas debe retornar HTTP 401")
    void testLoginFallido() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        String json = "{\"username\":\"cliente\",\"password\":\"wrong\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    @DisplayName("GET /api/auth/roles debe retornar los tres roles del sistema")
    void testListarRoles() throws Exception {
        mockMvc.perform(get("/api/auth/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ADMIN"))
                .andExpect(jsonPath("$[1]").value("VENDEDOR"))
                .andExpect(jsonPath("$[2]").value("CLIENTE"));
    }
}
