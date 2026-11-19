package co.frameworks.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRequest {

    private String username;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefono;

    private String metodoPago;

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    private List<ItemPedidoRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemPedidoRequest {
        private Long productoId;
        private Integer cantidad;
        private String talla;
    }
}
