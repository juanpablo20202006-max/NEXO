package co.frameworks.tienda.service;

import co.frameworks.tienda.dto.SerpApiStoreDto;
import co.frameworks.tienda.model.TiendaFisica;
import co.frameworks.tienda.repository.TiendaFisicaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SerpApiService {

    private static final Logger log = LoggerFactory.getLogger(SerpApiService.class);

    private final TiendaFisicaRepository tiendaFisicaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${serpapi.api.key:demo_nexokey_2026}")
    private String apiKey;

    @Value("${serpapi.base-url:https://serpapi.com/search.json}")
    private String baseUrl;

    @Transactional(readOnly = true)
    public List<SerpApiStoreDto> buscarTiendas(String query, String location) {
        String querySearch = (query != null && !query.isBlank()) ? query.trim() : "tiendas de ropa";
        String locationSearch = (location != null && !location.isBlank()) ? location.trim() : "Villavicencio, Meta";

        log.info("Consultando tiendas cercanas con SerpAPI: query='{}', location='{}'", querySearch, locationSearch);

        // Si hay API key real configurada y no es demo, intentar consultar SerpAPI en vivo
        if (apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("demo_")) {
            try {
                String url = String.format("%s?engine=google_maps&q=%s&ll=%s&api_key=%s",
                        baseUrl,
                        querySearch + " " + locationSearch,
                        "@4.1420,-73.6266,14z",
                        apiKey);

                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);
                JsonNode localResults = root.path("local_results");

                if (localResults.isArray() && localResults.size() > 0) {
                    List<SerpApiStoreDto> externalResults = new ArrayList<>();
                    for (JsonNode item : localResults) {
                        externalResults.add(SerpApiStoreDto.builder()
                                .id(item.path("data_id").asText("ext-" + System.currentTimeMillis()))
                                .title(item.path("title").asText("Tienda NEXO"))
                                .address(item.path("address").asText("Ubicación comercial"))
                                .city(locationSearch)
                                .phone(item.path("phone").asText("+57 (8) 660-1234"))
                                .rating(item.path("rating").asDouble(4.5))
                                .reviews(item.path("reviews").asInt(120))
                                .openState(item.path("open_state").asText("Abierto"))
                                .thumbnail(item.path("thumbnail").asText("https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=500&q=80"))
                                .latitude(item.path("gps_coordinates").path("latitude").asDouble(4.1420))
                                .longitude(item.path("gps_coordinates").path("longitude").asDouble(-73.6266))
                                .placeId(item.path("place_id").asText(""))
                                .nexoRegistrado(item.path("title").asText("").toLowerCase().contains("nexo"))
                                .build());
                    }
                    return externalResults;
                }
            } catch (Exception e) {
                log.warn("Llamada a SerpAPI remota no disponible ({}), utilizando catálogo local de alta fidelidad.", e.getMessage());
            }
        }

        // Fallback resiliente y enriquecido desde base de datos PostgreSQL (TiendaFisica)
        List<TiendaFisica> locales = tiendaFisicaRepository.findAll();
        List<SerpApiStoreDto> dtos = new ArrayList<>();

        for (TiendaFisica t : locales) {
            boolean match = querySearch.equalsIgnoreCase("tiendas de ropa") ||
                    t.getNombre().toLowerCase().contains(querySearch.toLowerCase()) ||
                    t.getCiudad().toLowerCase().contains(querySearch.toLowerCase()) ||
                    querySearch.toLowerCase().contains("streetwear") ||
                    querySearch.toLowerCase().contains("ropa");

            if (match) {
                dtos.add(SerpApiStoreDto.builder()
                        .id(String.valueOf(t.getId()))
                        .title(t.getNombre())
                        .address(t.getDireccion())
                        .city(t.getCiudad())
                        .phone(t.getTelefono())
                        .rating(t.getRating())
                        .reviews(t.getTotalReviews())
                        .openState(t.getHorario())
                        .thumbnail(t.getImagenUrl())
                        .latitude(t.getLatitud())
                        .longitude(t.getLongitud())
                        .placeId(t.getPlaceId())
                        .nexoRegistrado(Boolean.TRUE.equals(t.getNexoRegistrado()))
                        .build());
            }
        }

        return dtos;
    }
}
