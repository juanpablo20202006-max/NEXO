package co.frameworks.tienda.controller;

import co.frameworks.tienda.dto.SerpApiStoreDto;
import co.frameworks.tienda.service.SerpApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Pruebas del Controlador de Integración SerpAPI")
class SerpApiRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SerpApiService serpApiService;

    @Test
    @DisplayName("GET /api/serpapi/tiendas debe retornar tiendas geolocalizadas de ropa")
    void testBuscarTiendas() throws Exception {
        SerpApiStoreDto tienda = SerpApiStoreDto.builder()
                .id("1")
                .title("Alta Moda | NEXO TIENDA")
                .address("Avenida 40 No. 26-10")
                .city("Villavicencio, Meta")
                .rating(4.9)
                .reviews(342)
                .openState("Abierto")
                .nexoRegistrado(true)
                .build();

        when(serpApiService.buscarTiendas("tiendas de ropa", "Villavicencio, Meta"))
                .thenReturn(List.of(tienda));

        mockMvc.perform(get("/api/serpapi/tiendas")
                        .param("query", "tiendas de ropa")
                        .param("location", "Villavicencio, Meta")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Alta Moda | NEXO TIENDA"))
                .andExpect(jsonPath("$[0].nexoRegistrado").value(true));
    }
}
