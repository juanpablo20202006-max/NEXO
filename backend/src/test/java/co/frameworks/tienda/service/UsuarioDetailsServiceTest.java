package co.frameworks.tienda.service;

import co.frameworks.tienda.model.Rol;
import co.frameworks.tienda.model.Usuario;
import co.frameworks.tienda.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de UsuarioDetailsService")
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    @DisplayName("Debe cargar exitosamente un usuario ADMIN con credenciales y autoridad ROLE_ADMIN")
    void testLoadUserByUsernameAdmin() {
        Usuario usuario = new Usuario("admin", "$2a$10$encryptedPassAdmin", Rol.ADMIN);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertEquals("$2a$10$encryptedPassAdmin", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        // Verificar que el rol se mapee con el prefijo ROLE_ requerido por Spring Security
        boolean hasRoleAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        assertTrue(hasRoleAdmin, "Debe contener la autoridad ROLE_ADMIN");
        assertEquals(1, userDetails.getAuthorities().size());

        verify(usuarioRepository, times(1)).findByUsername("admin");
    }

    @Test
    @DisplayName("Debe cargar exitosamente un usuario VENDEDOR con autoridad ROLE_VENDEDOR")
    void testLoadUserByUsernameVendedor() {
        Usuario usuario = new Usuario("vendedor", "$2a$10$encryptedPassVend", Rol.VENDEDOR);
        when(usuarioRepository.findByUsername("vendedor")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("vendedor");

        assertNotNull(userDetails);
        assertEquals("vendedor", userDetails.getUsername());
        boolean hasRoleVendedor = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_VENDEDOR"));
        assertTrue(hasRoleVendedor, "Debe contener la autoridad ROLE_VENDEDOR");

        verify(usuarioRepository, times(1)).findByUsername("vendedor");
    }

    @Test
    @DisplayName("Debe cargar exitosamente un usuario CLIENTE con autoridad ROLE_CLIENTE")
    void testLoadUserByUsernameCliente() {
        Usuario usuario = new Usuario("cliente", "$2a$10$encryptedPassCli", Rol.CLIENTE);
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("cliente");

        assertNotNull(userDetails);
        assertEquals("cliente", userDetails.getUsername());
        boolean hasRoleCliente = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CLIENTE"));
        assertTrue(hasRoleCliente, "Debe contener la autoridad ROLE_CLIENTE");

        verify(usuarioRepository, times(1)).findByUsername("cliente");
    }

    @Test
    @DisplayName("Debe lanzar UsernameNotFoundException cuando el usuario no existe")
    void testLoadUserByUsernameNotFound() {
        when(usuarioRepository.findByUsername("no_existe")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername("no_existe")
        );

        assertEquals("Usuario no encontrado: no_existe", exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername("no_existe");
    }

    @Test
    @DisplayName("La instancia retornada debe ser org.springframework.security.core.userdetails.User y no la entidad JPA")
    void testReturnTypeIsSpringSecurityUser() {
        Usuario usuario = new Usuario("testuser", "pass", Rol.CLIENTE);
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("testuser");

        assertInstanceOf(org.springframework.security.core.userdetails.User.class, userDetails);
        assertNotSame(usuario, userDetails);
    }
}
