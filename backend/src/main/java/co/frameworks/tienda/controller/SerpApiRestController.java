package co.frameworks.tienda.controller;

import co.frameworks.tienda.dto.SerpApiStoreDto;
import co.frameworks.tienda.service.SerpApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/serpapi")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Integración SerpAPI", description = "Endpoints de geolocalización y búsqueda de tiendas físicas en Google Maps")
public class SerpApiRestController {

    private final SerpApiService serpApiService;

    @GetMapping("/tiendas")
    @Operation(summary = "Buscar tiendas de ropa cercanas utilizando el motor Google Maps de SerpAPI")
    public ResponseEntity<List<SerpApiStoreDto>> buscarTiendas(
            @RequestParam(required = false, defaultValue = "tiendas de ropa") String query,
            @RequestParam(required = false, defaultValue = "Villavicencio, Meta") String location
    ) {
        List<SerpApiStoreDto> tiendas = serpApiService.buscarTiendas(query, location);
        return ResponseEntity.ok(tiendas);
    }
}
