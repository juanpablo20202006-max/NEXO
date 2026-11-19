package co.frameworks.tienda.repository;

import co.frameworks.tienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Usuario}.
 *
 * Provee métodos CRUD estándar y métodos de consulta derivados para la autenticación
 * con Spring Security y validaciones de unicidad en registros de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario único.
     * Utilizado principalmente por {@code UserDetailsService} en la capa de seguridad.
     *
     * @param username Nombre de usuario a buscar
     * @return {@link Optional} con el usuario correspondiente si existe, o vacío en caso contrario
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Comprueba si ya existe un usuario registrado con el username provisto.
     * Utilizado para validación en formularios de registro y actualización.
     *
     * @param username Nombre de usuario a verificar
     * @return {@code true} si ya existe un usuario con dicho nombre, {@code false} de lo contrario
     */
    boolean existsByUsername(String username);
}
