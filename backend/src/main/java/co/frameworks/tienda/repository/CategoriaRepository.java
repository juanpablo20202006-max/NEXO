package co.frameworks.tienda.repository;

import co.frameworks.tienda.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findBySlug(String slug);
    Optional<Categoria> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    boolean existsBySlug(String slug);
}
