package co.frameworks.tienda.repository;

import co.frameworks.tienda.model.Rol;
import co.frameworks.tienda.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Pruebas de Integración de UsuarioRepository con PostgreSQL")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Debe persistir un usuario y recuperarlo mediante findByUsername")
    void testGuardarYFindByUsername() {
        Usuario usuario = new Usuario("admin_test", "$2a$10$D8hashAdminTest", Rol.ADMIN);
        Usuario guardado = usuarioRepository.save(usuario);

        assertNotNull(guardado.getId());
        assertEquals("admin_test", guardado.getUsername());
        assertEquals(Rol.ADMIN, guardado.getRol());

        Optional<Usuario> encontrado = usuarioRepository.findByUsername("admin_test");
        assertTrue(encontrado.isPresent());
        assertEquals("admin_test", encontrado.get().getUsername());
        assertEquals(Rol.ADMIN, encontrado.get().getRol());
    }

    @Test
    @DisplayName("existsByUsername debe retornar true si existe y false si no existe")
    void testExistsByUsername() {
        Usuario vendedor = new Usuario("vendedor_test", "passwordHashVendedor", Rol.VENDEDOR);
        usuarioRepository.save(vendedor);

        assertTrue(usuarioRepository.existsByUsername("vendedor_test"));
        assertFalse(usuarioRepository.existsByUsername("inexistente_user"));
    }

    @Test
    @DisplayName("Debe fallar al intentar persistir dos usuarios con el mismo username (restricción UNIQUE)")
    void testUniqueConstraintUsername() {
        Usuario u1 = new Usuario("duplicado", "pass1", Rol.CLIENTE);
        usuarioRepository.saveAndFlush(u1);

        Usuario u2 = new Usuario("duplicado", "pass2", Rol.CLIENTE);
        assertThrows(DataIntegrityViolationException.class, () -> {
            usuarioRepository.saveAndFlush(u2);
        });
    }

    @Test
    @DisplayName("Debe persistir correctamente el Enum Rol como STRING en la base de datos")
    void testRolPersistedAsString() {
        Usuario cliente = new Usuario("cliente_test", "passClient", Rol.CLIENTE);
        Usuario guardado = usuarioRepository.saveAndFlush(cliente);

        Optional<Usuario> recuperado = usuarioRepository.findById(guardado.getId());
        assertTrue(recuperado.isPresent());
        assertEquals(Rol.CLIENTE, recuperado.get().getRol());
    }
}
