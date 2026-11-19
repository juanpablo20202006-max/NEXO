package co.frameworks.tienda.controller;

import co.frameworks.tienda.config.SecurityConfig;
import co.frameworks.tienda.repository.UsuarioRepository;
import co.frameworks.tienda.service.UsuarioDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración de capa Web/MVC para {@link PaginaController}.
 *
 * Valida la resolución de plantillas Thymeleaf, las reglas de seguridad
 * y la redirección inteligente basada en las autoridades del usuario autenticado.
 */
@WebMvcTest(PaginaController.class)
@Import(SecurityConfig.class)
@DisplayName("Pruebas Unitarias e Integración - PaginaController")
class PaginaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Nested
    @DisplayName("Pruebas de Rutas Públicas y Renderizado de Vistas")
    class RutasPublicasTests {

        @Test
        @DisplayName("GET '/' debe responder 200 OK y resolver la plantilla 'index'")
        void testIndexView() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"))
                    .andExpect(content().string(containsString("Sistema de Gestión y Comercio Electrónico")));
        }

        @Test
        @DisplayName("GET '/login' debe responder 200 OK y resolver la plantilla 'login'")
        void testLoginView() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"))
                    .andExpect(content().string(containsString("Iniciar Sesión")));
        }

        @Test
        @DisplayName("GET '/login?error' debe mostrar el mensaje de error de credenciales")
        void testLoginErrorView() throws Exception {
            mockMvc.perform(get("/login").param("error", "true"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"))
                    .andExpect(content().string(containsString("Usuario o contraseña incorrectos.")));
        }

        @Test
        @DisplayName("GET '/login?logout' debe mostrar el mensaje de éxito de cierre de sesión")
        void testLoginLogoutView() throws Exception {
            mockMvc.perform(get("/login").param("logout", "true"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"))
                    .andExpect(content().string(containsString("Has cerrado sesión correctamente.")));
        }

        @Test
        @DisplayName("GET '/acceso-denegado' debe responder 200 OK y resolver la plantilla 'acceso-denegado'")
        void testAccesoDenegadoView() throws Exception {
            mockMvc.perform(get("/acceso-denegado"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("acceso-denegado"))
                    .andExpect(content().string(containsString("403")))
                    .andExpect(content().string(containsString("Acceso Denegado")));
        }
    }

    @Nested
    @DisplayName("Pruebas de Seguridad en Rutas Protegidas (Anónimo -> Redirección a Login)")
    class RutasProtegidasAnonimoTests {

        @Test
        @DisplayName("GET '/cliente' anónimo debe redirigir a '/login'")
        void testClienteRequiereAutenticacion() throws Exception {
            mockMvc.perform(get("/cliente"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("GET '/vendedor' anónimo debe redirigir a '/login'")
        void testVendedorRequiereAutenticacion() throws Exception {
            mockMvc.perform(get("/vendedor"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @DisplayName("GET '/admin' anónimo debe redirigir a '/login'")
        void testAdminRequiereAutenticacion() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Nested
    @DisplayName("Pruebas de Acceso por Rol a Vistas Protegidas")
    class RutasPorRolTests {

        @Test
        @WithMockUser(username = "cliente", roles = {"CLIENTE"})
        @DisplayName("GET '/cliente' con ROLE_CLIENTE responde 200 y resuelve plantilla 'cliente'")
        void testClientePermitidoParaCliente() throws Exception {
            mockMvc.perform(get("/cliente"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cliente"))
                    .andExpect(content().string(containsString("Panel del Cliente")));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("GET '/cliente' con ROLE_ADMIN responde 200 y resuelve plantilla 'cliente'")
        void testClientePermitidoParaAdmin() throws Exception {
            mockMvc.perform(get("/cliente"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cliente"))
                    .andExpect(content().string(containsString("Panel del Cliente")));
        }

        @Test
        @WithMockUser(username = "vendedor", roles = {"VENDEDOR"})
        @DisplayName("GET '/cliente' con ROLE_VENDEDOR es denegado (403 Forbidden)")
        void testClienteDenegadoParaVendedor() throws Exception {
            mockMvc.perform(get("/cliente"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "vendedor", roles = {"VENDEDOR"})
        @DisplayName("GET '/vendedor' con ROLE_VENDEDOR responde 200 y resuelve plantilla 'vendedor'")
        void testVendedorPermitidoParaVendedor() throws Exception {
            mockMvc.perform(get("/vendedor"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("vendedor"))
                    .andExpect(content().string(containsString("Panel del Vendedor")));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("GET '/vendedor' con ROLE_ADMIN responde 200 y resuelve plantilla 'vendedor'")
        void testVendedorPermitidoParaAdmin() throws Exception {
            mockMvc.perform(get("/vendedor"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("vendedor"))
                    .andExpect(content().string(containsString("Panel del Vendedor")));
        }

        @Test
        @WithMockUser(username = "cliente", roles = {"CLIENTE"})
        @DisplayName("GET '/vendedor' con ROLE_CLIENTE es denegado (403 Forbidden)")
        void testVendedorDenegadoParaCliente() throws Exception {
            mockMvc.perform(get("/vendedor"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("GET '/admin' con ROLE_ADMIN responde 200 y resuelve plantilla 'admin'")
        void testAdminPermitidoParaAdmin() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin"))
                    .andExpect(content().string(containsString("Panel Administrativo")));
        }

        @Test
        @WithMockUser(username = "vendedor", roles = {"VENDEDOR"})
        @DisplayName("GET '/admin' con ROLE_VENDEDOR es denegado (403 Forbidden)")
        void testAdminDenegadoParaVendedor() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "cliente", roles = {"CLIENTE"})
        @DisplayName("GET '/admin' con ROLE_CLIENTE es denegado (403 Forbidden)")
        void testAdminDenegadoParaCliente() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Pruebas de Redirección Inteligente Post-Login ('/redirigir')")
    class RedireccionTests {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("GET '/redirigir' con ROLE_ADMIN debe redirigir a '/admin'")
        void testRedirigirAdmin() throws Exception {
            mockMvc.perform(get("/redirigir"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin"));
        }

        @Test
        @WithMockUser(username = "vendedor", roles = {"VENDEDOR"})
        @DisplayName("GET '/redirigir' con ROLE_VENDEDOR debe redirigir a '/vendedor'")
        void testRedirigirVendedor() throws Exception {
            mockMvc.perform(get("/redirigir"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/vendedor"));
        }

        @Test
        @WithMockUser(username = "cliente", roles = {"CLIENTE"})
        @DisplayName("GET '/redirigir' con ROLE_CLIENTE debe redirigir a '/cliente'")
        void testRedirigirCliente() throws Exception {
            mockMvc.perform(get("/redirigir"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cliente"));
        }

        @Test
        @WithMockUser(username = "usuario_invitado", roles = {"OTRO_ROL"})
        @DisplayName("GET '/redirigir' con rol no mapeado debe redirigir a '/' por defecto")
        void testRedirigirPorDefecto() throws Exception {
            mockMvc.perform(get("/redirigir"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }

        @Test
        @DisplayName("GET '/redirigir' anónimo debe redirigir a '/login' debido a la regla anyRequest().authenticated()")
        void testRedirigirAnonimoRequiereAutenticacion() throws Exception {
            mockMvc.perform(get("/redirigir"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Nested
    @DisplayName("Pruebas de Cierre de Sesión (POST /logout)")
    class LogoutTests {

        @Test
        @WithMockUser(username = "cualquiera", roles = {"CLIENTE"})
        @DisplayName("POST '/logout' con CSRF válido debe invalidar la sesión y redirigir a '/'")
        void testLogoutExitoso() throws Exception {
            mockMvc.perform(post("/logout").with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }
    }
}
