package co.frameworks.tienda.service;

import co.frameworks.tienda.dto.PedidoRequest;
import co.frameworks.tienda.model.DetallePedido;
import co.frameworks.tienda.model.Pedido;
import co.frameworks.tienda.model.Producto;
import co.frameworks.tienda.model.Usuario;
import co.frameworks.tienda.repository.PedidoRepository;
import co.frameworks.tienda.repository.ProductoRepository;
import co.frameworks.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        return pedidoRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional
    public Pedido crearPedido(PedidoRequest request, String fallbackUsername) {
        String rawUsername = (request.getUsername() != null && !request.getUsername().isBlank())
                ? request.getUsername()
                : fallbackUsername;

        final String finalUsername = (rawUsername == null || rawUsername.isBlank())
                ? "cliente"
                : rawUsername;

        Usuario usuario = usuarioRepository.findByUsername(finalUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para el pedido: " + finalUsername));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto.");
        }

        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .numeroPedido("NX-" + System.currentTimeMillis())
                .fechaCreacion(LocalDateTime.now())
                .estado("CONFIRMADO")
                .direccionEntrega(request.getDireccionEntrega())
                .ciudad(request.getCiudad())
                .telefono(request.getTelefono())
                .metodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : "CONTRA_ENTREGA")
                .total(BigDecimal.ZERO)
                .detalles(new ArrayList<>())
                .build();

        BigDecimal granTotal = BigDecimal.ZERO;

        for (PedidoRequest.ItemPedidoRequest item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + item.getProductoId()));

            int cantidad = (item.getCantidad() != null && item.getCantidad() > 0) ? item.getCantidad() : 1;

            if (producto.getStock() < cantidad) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre() + ". Disponible: " + producto.getStock());
            }

            // Descontar inventario
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            granTotal = granTotal.add(subtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(cantidad)
                    .tallaSeleccionada(item.getTalla() != null ? item.getTalla() : "M")
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build();

            pedido.getDetalles().add(detalle);
        }

        pedido.setTotal(granTotal);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + id));
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }
}
