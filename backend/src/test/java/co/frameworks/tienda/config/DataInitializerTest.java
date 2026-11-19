package co.frameworks.tienda.config;

import co.frameworks.tienda.model.Rol;
import co.frameworks.tienda.model.Usuario;
import co.frameworks.tienda.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de DataInitializer")
class DataInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("Debe inicializar los 3 usuarios académicos con contraseñas cifradas cuando la BD está vacía")
    void testRunInitializesUsersWhenNotPresent() {
        // Ninguno de los usuarios existe inicialmente
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("$2a$10$encodedAdmin123");
        when(passwordEncoder.encode("vend123")).thenReturn("$2a$10$encodedVend123");
        when(passwordEncoder.encode("cli123")).thenReturn("$2a$10$encodedCli123");

        dataInitializer.run();

        // Verificar que se comprobaron los 3 usuarios
        verify(usuarioRepository, times(1)).existsByUsername("admin");
        verify(usuarioRepository, times(1)).existsByUsername("vendedor");
        verify(usuarioRepository, times(1)).existsByUsername("cliente");

        // Capturar los usuarios guardados
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(3)).save(usuarioCaptor.capture());

        List<Usuario> savedUsers = usuarioCaptor.getAllValues();
        assertEquals(3, savedUsers.size());

        // Verificar usuario admin
        Usuario admin = savedUsers.stream().filter(u -> u.getUsername().equals("admin")).findFirst().orElseThrow();
        assertEquals("$2a$10$encodedAdmin123", admin.getPassword());
        assertEquals(Rol.ADMIN, admin.getRol());

        // Verificar usuario vendedor
        Usuario vendedor = savedUsers.stream().filter(u -> u.getUsername().equals("vendedor")).findFirst().orElseThrow();
        assertEquals("$2a$10$encodedVend123", vendedor.getPassword());
        assertEquals(Rol.VENDEDOR, vendedor.getRol());

        // Verificar usuario cliente
        Usuario cliente = savedUsers.stream().filter(u -> u.getUsername().equals("cliente")).findFirst().orElseThrow();
        assertEquals("$2a$10$encodedCli123", cliente.getPassword());
        assertEquals(Rol.CLIENTE, cliente.getRol());
    }

    @Test
    @DisplayName("No debe duplicar usuarios si ya existen en la base de datos (idempotencia)")
    void testRunDoesNotDuplicateExistingUsers() {
        // Simular que todos los usuarios ya existen
        when(usuarioRepository.existsByUsername("admin")).thenReturn(true);
        when(usuarioRepository.existsByUsername("vendedor")).thenReturn(true);
        when(usuarioRepository.existsByUsername("cliente")).thenReturn(true);

        dataInitializer.run();

        verify(usuarioRepository, times(1)).existsByUsername("admin");
        verify(usuarioRepository, times(1)).existsByUsername("vendedor");
        verify(usuarioRepository, times(1)).existsByUsername("cliente");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Debe crear selectivamente solo el usuario que falte")
    void testRunCreatesOnlyMissingUsers() {
        when(usuarioRepository.existsByUsername("admin")).thenReturn(true);
        when(usuarioRepository.existsByUsername("vendedor")).thenReturn(false);
        when(usuarioRepository.existsByUsername("cliente")).thenReturn(true);
        when(passwordEncoder.encode("vend123")).thenReturn("$2a$10$encodedVend123");

        dataInitializer.run();

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, times(1)).encode("vend123");
    }
}
