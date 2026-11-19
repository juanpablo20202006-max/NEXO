package co.frameworks.tienda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad JPA que representa la tabla de usuarios en PostgreSQL.
 *
 * NOTA DE ARQUITECTURA:
 * La entidad se denomina explícitamente "Usuario" (y no "User") para evitar colisiones
 * y ambigüedades con la clase {@code org.springframework.security.core.userdetails.User}.
 * La tabla subyacente se nombra explícitamente "usuario" para prevenir conflictos con la
 * palabra reservada "user" en PostgreSQL y otros motores SQL estándar.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    /**
     * Constructor de conveniencia para la creación e inserción de nuevos usuarios.
     *
     * @param username Nombre de usuario único para credenciales
     * @param password Contraseña cifrada (BCrypt)
     * @param rol Rol de autorización asignado (ADMIN, VENDEDOR, CLIENTE)
     */
    public Usuario(String username, String password, Rol rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(username, usuario.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
