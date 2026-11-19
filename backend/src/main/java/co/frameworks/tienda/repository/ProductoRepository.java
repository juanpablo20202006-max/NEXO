package co.frameworks.tienda.repository;

import co.frameworks.tienda.model.Categoria;
import co.frameworks.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySlug(String slug);
    List<Producto> findByCategoria(Categoria categoria);
    List<Producto> findByCategoriaSlug(String slug);
    List<Producto> findByNombreContainingIgnoreCase(String query);
    List<Producto> findByDestacadoTrue();
    boolean existsBySlug(String slug);
}
