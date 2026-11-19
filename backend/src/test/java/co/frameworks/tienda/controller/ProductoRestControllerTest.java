package co.frameworks.tienda.controller;

import co.frameworks.tienda.model.Categoria;
import co.frameworks.tienda.model.Producto;
import co.frameworks.tienda.service.ProductoService;
import co.frameworks.tienda.service.UsuarioDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas del Controlador REST de Productos NEXO")
class ProductoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Test
    @DisplayName("GET /api/productos debe retornar lista de prendas con HTTP 200")
    void testListarProductos() throws Exception {
        Categoria cat = new Categoria("Camisetas", "camisetas", "Prendas urbanas");
        Producto p1 = Producto.builder()
                .id(1L)
                .nombre("Camiseta NEXO 01")
                .slug("camiseta-nexo-01")
                .precio(new BigDecimal("79000.00"))
                .stock(20)
                .categoria(cat)
                .build();

        when(productoService.listarTodos()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Camiseta NEXO 01"))
                .andExpect(jsonPath("$[0].slug").value("camiseta-nexo-01"));
    }

    @Test
    @DisplayName("GET /api/productos/{id} debe retornar el producto solicitado")
    void testBuscarPorId() throws Exception {
        Categoria cat = new Categoria("Hoodies", "hoodies", "Buzos");
        Producto p = Producto.builder()
                .id(5L)
                .nombre("Hoodie NEXO Core Black")
                .slug("hoodie-nexo-core-black")
                .precio(new BigDecimal("145000.00"))
                .stock(10)
                .categoria(cat)
                .build();

        when(productoService.buscarPorId(5L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/productos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Hoodie NEXO Core Black"));
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{slug} debe filtrar por categoría")
    void testListarPorCategoria() throws Exception {
        Categoria cat = new Categoria("Camisetas", "camisetas", "Prendas urbanas");
        Producto p = Producto.builder()
                .id(2L)
                .nombre("Camiseta NEXO 02")
                .slug("camiseta-nexo-02")
                .precio(new BigDecimal("82000.00"))
                .stock(15)
                .categoria(cat)
                .build();

        when(productoService.buscarPorCategoriaSlug("camisetas")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/productos/categoria/camisetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("camiseta-nexo-02"));
    }
}
