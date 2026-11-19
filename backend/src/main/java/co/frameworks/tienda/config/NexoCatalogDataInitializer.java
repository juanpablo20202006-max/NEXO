package co.frameworks.tienda.config;

import co.frameworks.tienda.model.Categoria;
import co.frameworks.tienda.model.Producto;
import co.frameworks.tienda.model.TiendaFisica;
import co.frameworks.tienda.repository.CategoriaRepository;
import co.frameworks.tienda.repository.ProductoRepository;
import co.frameworks.tienda.repository.TiendaFisicaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(2)
@RequiredArgsConstructor
public class NexoCatalogDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(NexoCatalogDataInitializer.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final TiendaFisicaRepository tiendaFisicaRepository;

    @Override
    public void run(String... args) {
        log.info("Inicializando catálogo comercial y tiendas físicas NEXO...");
        inicializarCategorias();
        inicializarProductos();
        inicializarTiendasFisicas();
        log.info("Inicialización de catálogo y geolocalización NEXO completada exitosamente.");
    }

    private void inicializarCategorias() {
        crearCategoriaSiNoExiste("Camisetas", "camisetas", "Prendas urbanas en algodón premium con cortes oversize y regular fit.");
        crearCategoriaSiNoExiste("Hoodies", "hoodies", "Buzos y sudaderas con capota de alto gramaje para máxima durabilidad.");
        crearCategoriaSiNoExiste("Polos", "polos", "Camisas tipo polo de estilo contemporáneo y tejido piqué estructurado.");
        crearCategoriaSiNoExiste("Billeteras", "billeteras", "Accesorios minimalistas en cuero genuino y materiales sintéticos sostenibles.");
        crearCategoriaSiNoExiste("Gorras", "gorras", "Gorras streetwear de 5 y 6 paneles con bordados en relieve.");
    }

    private void crearCategoriaSiNoExiste(String nombre, String slug, String descripcion) {
        if (!categoriaRepository.existsBySlug(slug)) {
            Categoria cat = Categoria.builder()
                    .nombre(nombre)
                    .slug(slug)
                    .descripcion(descripcion)
                    .activa(true)
                    .build();
            categoriaRepository.save(cat);
            log.info("Categoría creada: {}", nombre);
        }
    }

    private void inicializarProductos() {
        Categoria camisetas = categoriaRepository.findBySlug("camisetas").orElse(null);
        Categoria hoodies = categoriaRepository.findBySlug("hoodies").orElse(null);
        Categoria polos = categoriaRepository.findBySlug("polos").orElse(null);
        Categoria billeteras = categoriaRepository.findBySlug("billeteras").orElse(null);
        Categoria gorras = categoriaRepository.findBySlug("gorras").orElse(null);

        if (camisetas != null) {
            crearProductoSiNoExiste(
                    "Camiseta NEXO 01",
                    "camiseta-nexo-01",
                    "Camiseta oversize color negro con estampado gráfico NEXO en serigrafía de alta densidad.",
                    new BigDecimal("79000.00"),
                    25,
                    "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=600&q=80",
                    "S, M, L, XL",
                    true,
                    camisetas
            );

            crearProductoSiNoExiste(
                    "Camiseta NEXO 02",
                    "camiseta-nexo-02",
                    "Camiseta regular fit en blanco marfil con micrologo bordado en el pecho.",
                    new BigDecimal("82000.00"),
                    18,
                    "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=600&q=80",
                    "S, M, L, XL",
                    true,
                    camisetas
            );

            crearProductoSiNoExiste(
                    "Camiseta NEXO 03",
                    "camiseta-nexo-03",
                    "Camiseta streetwear acid wash gris grafito con corte drapeado moderno.",
                    new BigDecimal("85000.00"),
                    14,
                    "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=600&q=80",
                    "S, M, L, XL",
                    false,
                    camisetas
            );

            crearProductoSiNoExiste(
                    "Camiseta NEXO 04",
                    "camiseta-nexo-04",
                    "Camiseta edición especial Cyber Gothic con ilustración trasera completa.",
                    new BigDecimal("89000.00"),
                    10,
                    "https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=600&q=80",
                    "S, M, L, XL",
                    true,
                    camisetas
            );
        }

        if (hoodies != null) {
            crearProductoSiNoExiste(
                    "Hoodie NEXO Core Black",
                    "hoodie-nexo-core-black",
                    "Buzo hoodie oversize negro en felpa perchada de 380 gramos con bolsillo canguro.",
                    new BigDecimal("145000.00"),
                    12,
                    "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=600&q=80",
                    "S, M, L, XL",
                    true,
                    hoodies
            );
        }

        if (polos != null) {
            crearProductoSiNoExiste(
                    "Polo NEXO Monogram",
                    "polo-nexo-monogram",
                    "Camisa polo pique slim fit con cuello camisero reforzado y botones personalizados.",
                    new BigDecimal("95000.00"),
                    15,
                    "https://images.unsplash.com/photo-1625910513413-5b8637c35f79?w=600&q=80",
                    "S, M, L, XL",
                    false,
                    polos
            );
        }

        if (billeteras != null) {
            crearProductoSiNoExiste(
                    "Billetera NEXO Stealth Leather",
                    "billetera-nexo-stealth-leather",
                    "Billetera bifold en cuero genuino con protección RFID anti-escaneo.",
                    new BigDecimal("65000.00"),
                    20,
                    "https://images.unsplash.com/photo-1627123424574-724758594e93?w=600&q=80",
                    "Única",
                    false,
                    billeteras
            );
        }

        if (gorras != null) {
            crearProductoSiNoExiste(
                    "Gorra NEXO Flat Snapback",
                    "gorra-nexo-flat-snapback",
                    "Gorra snapback de 6 paneles con visera plana y broche ajustable posterior.",
                    new BigDecimal("55000.00"),
                    22,
                    "https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=600&q=80",
                    "Ajustable",
                    false,
                    gorras
            );
        }
    }

    private void crearProductoSiNoExiste(String nombre, String slug, String descripcion, BigDecimal precio,
                                        int stock, String imagenUrl, String tallas, boolean destacado, Categoria categoria) {
        if (!productoRepository.existsBySlug(slug)) {
            Producto p = Producto.builder()
                    .nombre(nombre)
                    .slug(slug)
                    .descripcion(descripcion)
                    .precio(precio)
                    .stock(stock)
                    .imagenUrl(imagenUrl)
                    .tallas(tallas)
                    .destacado(destacado)
                    .categoria(categoria)
                    .build();
            productoRepository.save(p);
            log.info("Producto creado: {}", nombre);
        }
    }

    private void inicializarTiendasFisicas() {
        if (tiendaFisicaRepository.count() == 0) {
            tiendaFisicaRepository.save(TiendaFisica.builder()
                    .nombre("Mattelsa Villavicencio | CC Viva Villavicencio")
                    .direccion("Calle 7 No. 45-185, Centro Comercial Viva, Local 214")
                    .ciudad("Villavicencio, Meta")
                    .telefono("+57 (8) 668-9000")
                    .rating(4.5)
                    .totalReviews(557)
                    .horario("Abierto · Cierra a las 20:00")
                    .imagenUrl("https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?w=500&q=80")
                    .latitud(4.1255)
                    .longitud(-73.6335)
                    .placeId("ChIJq_mattelsa_viva")
                    .nexoRegistrado(false)
                    .build());

            tiendaFisicaRepository.save(TiendaFisica.builder()
                    .nombre("Tierra Santa Villavicencio")
                    .direccion("Carrera 31 No. 38-25, Centro Histórico")
                    .ciudad("Villavicencio, Meta")
                    .telefono("+57 (8) 662-1144")
                    .rating(4.3)
                    .totalReviews(616)
                    .horario("Abierto · Cierra a las 19:30")
                    .imagenUrl("https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=500&q=80")
                    .latitud(4.1488)
                    .longitud(-73.6360)
                    .placeId("ChIJ_tierrasanta_villa")
                    .nexoRegistrado(false)
                    .build());

            tiendaFisicaRepository.save(TiendaFisica.builder()
                    .nombre("Alta Moda | NEXO TIENDA")
                    .direccion("Avenida 40 No. 26-10, Plaza Barzal")
                    .ciudad("Villavicencio, Meta")
                    .telefono("+57 312 450 8899")
                    .rating(4.9)
                    .totalReviews(342)
                    .horario("Abierto · Cierra a las 21:00")
                    .imagenUrl("https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=500&q=80")
                    .latitud(4.1370)
                    .longitud(-73.6310)
                    .placeId("ChIJ_nexo_tienda_oficial")
                    .nexoRegistrado(true)
                    .build());

            tiendaFisicaRepository.save(TiendaFisica.builder()
                    .nombre("Maleja Clothing Streetwear")
                    .direccion("Carrera 40 No. 32-15, Barrio Siete de Agosto")
                    .ciudad("Villavicencio, Meta")
                    .telefono("+57 320 891 2233")
                    .rating(4.7)
                    .totalReviews(15)
                    .horario("Abierto · Cierra a las 20:00")
                    .imagenUrl("https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=500&q=80")
                    .latitud(4.1402)
                    .longitud(-73.6291)
                    .placeId("ChIJ_maleja_clothing")
                    .nexoRegistrado(false)
                    .build());

            tiendaFisicaRepository.save(TiendaFisica.builder()
                    .nombre("NEXO Flagship Store Bogotá - Zona T")
                    .direccion("Calle 82 No. 12-35, Zona Rosa")
                    .ciudad("Bogotá, Cundinamarca")
                    .telefono("+57 (1) 745-6620")
                    .rating(4.9)
                    .totalReviews(890)
                    .horario("Abierto · Cierra a las 22:00")
                    .imagenUrl("https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?w=500&q=80")
                    .latitud(4.6675)
                    .longitud(-74.0560)
                    .placeId("ChIJ_nexo_bogota_zonat")
                    .nexoRegistrado(true)
                    .build());

            log.info("5 tiendas físicas insertadas correctamente con datos de geolocalización.");
        }
    }
}
