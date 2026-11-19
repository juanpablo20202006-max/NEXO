package co.frameworks.tienda.repository;

import co.frameworks.tienda.model.TiendaFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TiendaFisicaRepository extends JpaRepository<TiendaFisica, Long> {
    List<TiendaFisica> findByCiudadIgnoreCase(String ciudad);
    List<TiendaFisica> findByNombreContainingIgnoreCaseOrDireccionContainingIgnoreCase(String q1, String q2);
    List<TiendaFisica> findByNexoRegistradoTrue();
}
