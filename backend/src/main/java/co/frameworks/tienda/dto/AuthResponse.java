package co.frameworks.tienda.dto;

import co.frameworks.tienda.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private boolean authenticated;
    private String message;
    private Long id;
    private String username;
    private Rol rol;
    private String token;
}
