package co.frameworks.tienda.controller;

import co.frameworks.tienda.dto.ProductoDto;
import co.frameworks.tienda.model.Producto;
import co.frameworks.tienda.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Endpoints para consulta, filtrado y gestión del catálogo de prendas NEXO")
public class ProductoRestController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Consultar catálogo completo o buscar prendas por texto")
    public ResponseEntity<List<Producto>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria
    ) {
        if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("todo")) {
            return ResponseEntity.ok(productoService.buscarPorCategoriaSlug(categoria));
        }
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(productoService.buscarPorTexto(search));
        }
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de un producto por su ID")
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Obtener el detalle de un producto por su slug")
    public ResponseEntity<Producto> buscarPorSlug(@PathVariable String slug) {
        return productoService.buscarPorSlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{catSlug}")
    @Operation(summary = "Filtrar prendas por slug de categoría (ej. camisetas, hoodies, polos)")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable String catSlug) {
        if (catSlug.equalsIgnoreCase("todo")) {
            return ResponseEntity.ok(productoService.listarTodos());
        }
        return ResponseEntity.ok(productoService.buscarPorCategoriaSlug(catSlug));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto en el catálogo (Requiere rol VENDEDOR o ADMIN)")
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoDto dto) {
        Producto creado = productoService.crearProducto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar información o stock de un producto existente")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDto dto) {
        Producto actualizado = productoService.actualizarProducto(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto del catálogo (Requiere rol ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
