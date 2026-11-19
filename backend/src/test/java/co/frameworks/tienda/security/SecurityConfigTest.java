package co.frameworks.tienda.security;

import co.frameworks.tienda.config.SecurityConfig;
import co.frameworks.tienda.repository.UsuarioRepository;
import co.frameworks.tienda.service.UsuarioDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityConfigTest.TestSecurityController.DummyController.class)
@Import({SecurityConfig.class, SecurityConfigTest.TestSecurityController.class})
@DisplayName("Pruebas de Configuración de Seguridad y Control de Acceso")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    /**
     * Controlador de prueba para verificar las reglas de autorización en los diferentes paths.
     */
    @TestConfiguration
    static class TestSecurityController {

        @RestController
        static class DummyController {
            @GetMapping("/")
            public String index() {
                return "index-ok";
            }

            @GetMapping("/login")
            public String login() {
                return "login-ok";
            }

            @GetMapping("/acceso-denegado")
            public String accesoDenegado() {
                return "acceso-denegado-ok";
            }

            @GetMapping("/admin/panel")
            public String adminPanel() {
                return "admin-ok";
            }

            @GetMapping("/vendedor/catalogo")
            public String vendedorCatalogo() {
                return "vendedor-ok";
            }

            @GetMapping("/cliente/pedidos")
            public String clientePedidos() {
                return "cliente-ok";
            }

            @GetMapping("/otro/privado")
            public String privado() {
                return "privado-ok";
            }
        }
    }

    @Nested
    @DisplayName("Pruebas de PasswordEncoder y BCrypt")
    class PasswordEncoderTests {

        @Test
        @DisplayName("El bean PasswordEncoder debe ser una instancia de BCryptPasswordEncoder")
        void testPasswordEncoderInstance() {
            assertNotNull(passwordEncoder);
            assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
        }

        @Test
        @DisplayName("BCrypt debe codificar y verificar correctamente contraseñas")
        void testBCryptEncodingAndMatching() {
            String rawPassword = "adminPassword123";
            String encoded = passwordEncoder.encode(rawPassword);

            assertNotNull(encoded);
            assertNotEquals(rawPassword, encoded);
            assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$"));
            assertTrue(passwordEncoder.matches(rawPassword, encoded));
            assertFalse(passwordEncoder.matches("passwordErroneo", encoded));
        }

        @Test
        @DisplayName("Dos codificaciones de la misma contraseña deben producir hashes distintos debido al salting aleatorio")
        void testBCryptSalting() {
            String password = "mismoPassword123";
            String hash1 = passwordEncoder.encode(password);
            String hash2 = passwordEncoder.encode(password);

            assertNotEquals(hash1, hash2, "BCrypt debe generar diferentes hashes con sal aleatoria");
            assertTrue(passwordEncoder.matches(password, hash1));
            assertTrue(passwordEncoder.matches(password, hash2));
        }
    }

    @Nested
    @DisplayName("Pruebas de Rutas Públicas (permitAll)")
    class PublicRoutesTests {

        @Test
        @DisplayName("GET '/' debe ser accesible públicamente sin autenticación")
        void testRootIsPublic() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("index-ok"));
        }

        @Test
        @DisplayName("GET '/login' debe ser accesible públicamente sin autenticación")
        void testLoginIsPublic() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("login-ok"));
        }

        @Test
        @DisplayName("GET '/acceso-denegado' debe ser accesible públicamente sin autenticación")
        void testAccesoDenegadoIsPublic() throws Exception {
            mockMvc.perform(get("/acceso-denegado"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("acceso-denegado-ok"));
        }
    }

    @Nested
    @DisplayName("Pruebas de Control de Acceso por Roles")
    class RoleBasedAccessTests {

        @Test
        @DisplayName("GET '/admin/**' sin autenticar debe redirigir al login")
        void testAdminRequiresAuthentication() throws Exception {
            mockMvc.perform(get("/admin/panel"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("GET '/admin/**' con rol ADMIN debe ser permitido (200 OK)")
        void testAdminAllowedForAdmin() throws Exception {
            mockMvc.perform(get("/admin/panel"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("admin-ok"));
        }

        @Test
        @WithMockUser(username = "vendedor", roles = {"VENDEDOR"})
        @DisplayName("GET '/admin/**' con rol VENDEDOR debe ser denegado (redirección a /acceso-denegado o 403)")
        void testAdminForbiddenForVendedor() throws Exception {
            mockMvc.perform(get("/admin/panel"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "cliente", roles = {"CLIENTE"})
        @DisplayName("GET '/admin/**' con rol CLIENTE debe ser denegado (403)")
        void testAdminForbiddenForCliente() throws Exception {
            mockMvc.perform(get("/admin/panel"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "vendedor", roles = {"VENDEDOR"})
        @DisplayName("GET '/vendedor/**' con rol VENDEDOR debe ser permitido (200 OK)")
        void testVendedorAllowedForVendedor() throws Exception {
            mockMvc.perform(get("/vendedor/catalogo"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("vendedor-ok"));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("GET '/vendedor/**' con rol ADMIN debe ser permitido (200 OK)")
        void testVendedorAllowedForAdmin() throws Exception {
            mockMvc.perform(get("/vendedor/catalogo"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("vendedor-ok"));
        }

        @Test
        @WithMockUser(username = "cliente", roles = {"CLIENTE"})
        @DisplayName("GET '/vendedor/**' con rol CLIENTE debe ser denegado (403)")
        void testVendedorForbiddenForCliente() throws Exception {
            mockMvc.perform(get("/vendedor/catalogo"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "cliente", roles = {"CLIENTE"})
        @DisplayName("GET '/cliente/**' con rol CLIENTE debe ser permitido (200 OK)")
        void testClienteAllowedForCliente() throws Exception {
            mockMvc.perform(get("/cliente/pedidos"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("cliente-ok"));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("GET '/cliente/**' con rol ADMIN debe ser permitido (200 OK)")
        void testClienteAllowedForAdmin() throws Exception {
            mockMvc.perform(get("/cliente/pedidos"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("cliente-ok"));
        }

        @Test
        @WithMockUser(username = "vendedor", roles = {"VENDEDOR"})
        @DisplayName("GET '/cliente/**' con rol VENDEDOR debe ser denegado (403)")
        void testClienteForbiddenForVendedor() throws Exception {
            mockMvc.perform(get("/cliente/pedidos"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET cualquier otra ruta sin autenticación debe redirigir al login")
        void testAnyRequestRequiresAuthentication() throws Exception {
            mockMvc.perform(get("/otro/privado"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Nested
    @DisplayName("Pruebas de Logout y Protección CSRF")
    class LogoutAndCsrfTests {

        @Test
        @WithMockUser(username = "usuario", roles = {"CLIENTE"})
        @DisplayName("POST '/logout' con token CSRF válido debe cerrar sesión y redirigir a '/'")
        void testLogoutWithCsrfSuccess() throws Exception {
            mockMvc.perform(post("/logout").with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }

        @Test
        @WithMockUser(username = "usuario", roles = {"CLIENTE"})
        @DisplayName("POST '/logout' SIN token CSRF debe ser rechazado con 403 Forbidden (CSRF habilitado)")
        void testLogoutWithoutCsrfForbidden() throws Exception {
            mockMvc.perform(post("/logout"))
                    .andExpect(status().isForbidden());
        }
    }
}
