package co.frameworks.tienda.service;

import co.frameworks.tienda.model.Usuario;
import co.frameworks.tienda.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación que implementa {@link UserDetailsService} de Spring Security.
 * Carga los detalles y credenciales del usuario desde la base de datos PostgreSQL
 * a través de {@link UsuarioRepository}.
 *
 * NOTA DE ARQUITECTURA:
 * La entidad de dominio {@link Usuario} no implementa {@code UserDetails} directamente,
 * preservando la separación de capas y evitando el acoplamiento del modelo JPA con el framework.
 * Se construye y retorna una instancia de {@link org.springframework.security.core.userdetails.User}.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}
