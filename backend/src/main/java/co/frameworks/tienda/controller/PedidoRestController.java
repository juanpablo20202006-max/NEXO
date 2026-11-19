package co.frameworks.tienda.controller;

import co.frameworks.tienda.dto.PedidoRequest;
import co.frameworks.tienda.model.Pedido;
import co.frameworks.tienda.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Pedidos y Carrito", description = "Endpoints para procesamiento de órdenes y compras en línea")
public class PedidoRestController {

    private final PedidoService pedidoService;

    @GetMapping
    @Operation(summary = "Listar todos los pedidos registrados en el sistema (Requiere VENDEDOR o ADMIN)")
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/mis-pedidos")
    @Operation(summary = "Consultar el historial de compras del cliente actual")
    public ResponseEntity<List<Pedido>> misPedidos(
            @RequestParam(required = false, defaultValue = "cliente") String username,
            Authentication authentication
    ) {
        String activeUser = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName()
                : username;
        return ResponseEntity.ok(pedidoService.listarPorUsuario(activeUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle consolidado de un pedido por ID")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear y procesar un nuevo pedido desde el carrito de compras")
    public ResponseEntity<?> crearPedido(@Valid @RequestBody PedidoRequest request, Authentication authentication) {
        String activeUser = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName()
                : request.getUsername();

        try {
            Pedido creado = pedidoService.crearPedido(request, activeUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de entrega o despacho de un pedido")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String nuevoEstado) {
        try {
            Pedido actualizado = pedidoService.actualizarEstado(id, nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
