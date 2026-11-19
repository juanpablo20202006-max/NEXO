package co.frameworks.tienda.controller;

import co.frameworks.tienda.dto.AuthResponse;
import co.frameworks.tienda.dto.LoginRequest;
import co.frameworks.tienda.dto.RegisterRequest;
import co.frameworks.tienda.model.Rol;
import co.frameworks.tienda.model.Usuario;
import co.frameworks.tienda.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login, registro y verificación de sesiones")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y validar credenciales")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras autenticación exitosa"));

            return ResponseEntity.ok(AuthResponse.builder()
                    .authenticated(true)
                    .message("Autenticación exitosa")
                    .id(usuario.getId())
                    .username(usuario.getUsername())
                    .rol(usuario.getRol())
                    .token("session-" + usuario.getUsername() + "-" + System.currentTimeMillis())
                    .build());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder()
                    .authenticated(false)
                    .message("Credenciales incorrectas: nombre de usuario o contraseña inválidos")
                    .build());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo cliente en el sistema")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El nombre de usuario '" + request.getUsername() + "' ya se encuentra registrado");
        }

        Usuario nuevo = new Usuario(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getRol() != null ? request.getRol() : Rol.CLIENTE
        );

        Usuario guardado = usuarioRepository.save(nuevo);

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .authenticated(true)
                .message("Usuario registrado exitosamente")
                .id(guardado.getId())
                .username(guardado.getUsername())
                .rol(guardado.getRol())
                .token("session-" + guardado.getUsername() + "-" + System.currentTimeMillis())
                .build());
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el usuario actualmente autenticado")
    public ResponseEntity<?> getMe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .authenticated(false)
                    .message("Usuario anónimo / no autenticado")
                    .build());
        }

        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        return ResponseEntity.ok(AuthResponse.builder()
                .authenticated(true)
                .message("Sesión activa")
                .id(usuario.getId())
                .username(usuario.getUsername())
                .rol(usuario.getRol())
                .build());
    }

    @GetMapping("/roles")
    @Operation(summary = "Listar los roles disponibles en el sistema")
    public ResponseEntity<List<Rol>> listarRoles() {
        return ResponseEntity.ok(Arrays.asList(Rol.values()));
    }
}
