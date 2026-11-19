package co.frameworks.tienda.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias del Modelo Usuario y Rol")
class UsuarioTest {

    @Test
    @DisplayName("Debe contener los tres roles requeridos: ADMIN, VENDEDOR, CLIENTE")
    void testRolEnumValues() {
        Rol[] roles = Rol.values();
        assertEquals(3, roles.length);
        assertEquals(Rol.ADMIN, Rol.valueOf("ADMIN"));
        assertEquals(Rol.VENDEDOR, Rol.valueOf("VENDEDOR"));
        assertEquals(Rol.CLIENTE, Rol.valueOf("CLIENTE"));
    }

    @Test
    @DisplayName("Debe instanciar Usuario con constructor conveniente y validar propiedades")
    void testUsuarioConvenienceConstructor() {
        Usuario usuario = new Usuario("juanpablo", "secretHash123", Rol.ADMIN);

        assertNull(usuario.getId());
        assertEquals("juanpablo", usuario.getUsername());
        assertEquals("secretHash123", usuario.getPassword());
        assertEquals(Rol.ADMIN, usuario.getRol());
    }

    @Test
    @DisplayName("Debe soportar patrón Builder y Setters de Lombok")
    void testUsuarioBuilderAndSetters() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("cliente1")
                .password("pass1234")
                .rol(Rol.CLIENTE)
                .build();

        assertEquals(1L, usuario.getId());
        assertEquals("cliente1", usuario.getUsername());
        assertEquals("pass1234", usuario.getPassword());
        assertEquals(Rol.CLIENTE, usuario.getRol());

        usuario.setRol(Rol.VENDEDOR);
        assertEquals(Rol.VENDEDOR, usuario.getRol());
    }

    @Test
    @DisplayName("El método toString no debe exponer la contraseña")
    void testToStringExcludesPassword() {
        Usuario usuario = new Usuario("seguro", "miPasswordSecreto", Rol.ADMIN);
        String str = usuario.toString();

        assertFalse(str.contains("miPasswordSecreto"), "toString no debe contener la contraseña en texto plano");
        assertTrue(str.contains("seguro"));
    }

    @Test
    @DisplayName("Validar equals y hashCode basados en id y username")
    void testEqualsAndHashCode() {
        Usuario u1 = Usuario.builder().id(10L).username("admin").build();
        Usuario u2 = Usuario.builder().id(10L).username("admin").build();
        Usuario u3 = Usuario.builder().id(20L).username("otro").build();

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
        assertNotEquals(u1, u3);
    }
}
