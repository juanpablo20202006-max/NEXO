package co.frameworks.tienda.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador web MVC principal para la navegación de la aplicación Tienda.
 *
 * Utiliza la anotación {@link Controller} (nunca @RestController) para despachar
 * las solicitudes HTTP hacia los nombres de plantillas Thymeleaf que se resuelven
 * en `src/main/resources/templates/`.
 */
@Controller
public class PaginaController {

    /**
     * Página de inicio pública con información general y accesos del sistema.
     *
     * @return Nombre de la vista "index"
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Formulario personalizado de inicio de sesión con soporte de CSRF.
     *
     * @return Nombre de la vista "login"
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Panel privado del rol CLIENTE (también accesible por ADMIN).
     *
     * @return Nombre de la vista "cliente"
     */
    @GetMapping("/cliente")
    public String cliente() {
        return "cliente";
    }

    /**
     * Panel privado del rol VENDEDOR (también accesible por ADMIN).
     *
     * @return Nombre de la vista "vendedor"
     */
    @GetMapping("/vendedor")
    public String vendedor() {
        return "vendedor";
    }

    /**
     * Panel privado exclusivo del rol ADMIN.
     *
     * @return Nombre de la vista "admin"
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    /**
     * Página de notificación de error HTTP 403 (Acceso Denegado).
     *
     * @return Nombre de la vista "acceso-denegado"
     */
    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }

    /**
     * Enrutador post-autenticación que evalúa las autoridades del usuario autenticado
     * y lo redirige automáticamente al panel correspondiente a su jerarquía de roles.
     *
     * Jerarquía de redirección:
     * 1. ROLE_ADMIN -> redirect:/admin
     * 2. ROLE_VENDEDOR -> redirect:/vendedor
     * 3. ROLE_CLIENTE -> redirect:/cliente
     * 4. En cualquier otro caso o usuario anónimo -> redirect:/
     *
     * @param authentication Objeto con los datos y autoridades del usuario en sesión
     * @return Instrucción de redirección de Spring MVC
     */
    @GetMapping("/redirigir")
    public String redirigir(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            boolean isVendedor = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_VENDEDOR".equals(a.getAuthority()));
            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

            if (isAdmin) {
                return "redirect:/admin";
            } else if (isVendedor) {
                return "redirect:/vendedor";
            } else if (isCliente) {
                return "redirect:/cliente";
            }
        }
        return "redirect:/";
    }
}
