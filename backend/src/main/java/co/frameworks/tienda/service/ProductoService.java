package co.frameworks.tienda.service;

import co.frameworks.tienda.dto.ProductoDto;
import co.frameworks.tienda.model.Categoria;
import co.frameworks.tienda.model.Producto;
import co.frameworks.tienda.repository.CategoriaRepository;
import co.frameworks.tienda.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorSlug(String slug) {
        return productoRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoriaSlug(String slug) {
        return productoRepository.findByCategoriaSlug(slug);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorTexto(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listarTodos();
        }
        return productoRepository.findByNombreContainingIgnoreCase(query.trim());
    }

    @Transactional
    public Producto crearProducto(ProductoDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + dto.getCategoriaId()));

        String slug = dto.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = generarSlug(dto.getNombre());
        }

        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .slug(slug)
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .imagenUrl(dto.getImagenUrl())
                .tallas(dto.getTallas() != null ? dto.getTallas() : "S, M, L, XL")
                .destacado(dto.getDestacado() != null ? dto.getDestacado() : false)
                .categoria(categoria)
                .build();

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarProducto(Long id, ProductoDto dto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + dto.getCategoriaId()));
            existente.setCategoria(categoria);
        }

        if (dto.getNombre() != null) existente.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) existente.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) existente.setPrecio(dto.getPrecio());
        if (dto.getStock() != null) existente.setStock(dto.getStock());
        if (dto.getImagenUrl() != null) existente.setImagenUrl(dto.getImagenUrl());
        if (dto.getTallas() != null) existente.setTallas(dto.getTallas());
        if (dto.getDestacado() != null) existente.setDestacado(dto.getDestacado());

        return productoRepository.save(existente);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    public static String generarSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
