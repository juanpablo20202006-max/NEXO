package co.frameworks.tienda.repository;

import co.frameworks.tienda.model.Pedido;
import co.frameworks.tienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    List<Pedido> findAllByOrderByFechaCreacionDesc();
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
}
