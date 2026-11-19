package co.frameworks.tienda.config;

import co.frameworks.tienda.model.Rol;
import co.frameworks.tienda.model.Usuario;
import co.frameworks.tienda.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos de prueba para la aplicación Tienda.
 * Se ejecuta al iniciar el contexto de Spring Boot e inserta los usuarios académicos
 * requeridos (admin, vendedor, cliente) con credenciales debidamente codificadas mediante BCrypt,
 * garantizando la idempotencia evitando duplicados.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Verificando existencia de usuarios iniciales en la base de datos...");
        crearUsuarioSiNoExiste("admin", "admin123", Rol.ADMIN);
        crearUsuarioSiNoExiste("vendedor", "vend123", Rol.VENDEDOR);
        crearUsuarioSiNoExiste("cliente", "cli123", Rol.CLIENTE);
        log.info("Inicialización de usuarios completada exitosamente.");
    }

    /**
     * Comprueba si el usuario ya existe en PostgreSQL mediante existsByUsername.
     * Si no existe, lo persiste con la contraseña cifrada en BCrypt.
     * Nunca expone contraseñas en logs ni en consola.
     *
     * @param username Nombre de usuario a verificar/crear
     * @param rawPassword Contraseña en texto plano a codificar con PasswordEncoder
     * @param rol Rol asignado al usuario
     */
    private void crearUsuarioSiNoExiste(String username, String rawPassword, Rol rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            String encryptedPassword = passwordEncoder.encode(rawPassword);
            Usuario usuario = new Usuario(username, encryptedPassword, rol);
            usuarioRepository.save(usuario);
            log.info("Usuario académico creado: username='{}', rol={}", username, rol);
        } else {
            log.debug("Usuario académico '{}' ya existe. No se realizaron cambios.", username);
        }
    }
}
