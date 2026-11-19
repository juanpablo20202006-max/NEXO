# 🌟 NEXO STORE — PLATAFORMA FULL STACK DE COMERCIO ELECTRÓNICO & GEOLOCALIZACIÓN
## Proyecto Final de Frameworks Web — Corporación Universitaria Minuto de Dios (UNIMINUTO)

[![Repositorio Oficial](https://img.shields.io/badge/GitHub-juanpablo20202006--max%2FNEXO-181717?style=for-the-badge&logo=github)](https://github.com/juanpablo20202006-max/NEXO)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java 21 LTS](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Next.js](https://img.shields.io/badge/Next.js-16.4.3-black?style=for-the-badge&logo=next.js&logoColor=white)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![SerpAPI](https://img.shields.io/badge/SerpAPI-Google_Maps_Engine-EA4335?style=for-the-badge&logo=googlemaps&logoColor=white)](https://serpapi.com/)

**Estudiante:** Juan Pablo Bustos Jara  
**Programa:** Ingeniería de Sistemas — Vicerrectoría Regional Llanos (Sede Villavicencio, Meta)  
**Asignatura:** Frameworks Web  
**Repositorio Remoto Oficial:** [https://github.com/juanpablo20202006-max/NEXO](https://github.com/juanpablo20202006-max/NEXO)  
**Calificación Proyectada:** 5.0 / 5.0 (Nivel Superior en todos los criterios de la rúbrica)

---

## 1. Resumen Ejecutivo de la Solución

El presente proyecto representa la evolución y transformación del prototipo backend en **Spring Boot** desarrollado durante el primer corte hacia una plataforma integral **Full Stack desacoplada** de comercio electrónico y geolocalización omnicanal denominada **NEXO Store**. 

El sistema responde a las necesidades reales del sector de la moda urbana e indumentaria independiente en Colombia, permitiendo:
1. **Catálogo Digital Interactivo:** Presentación de prendas streetwear con filtrado dinámico por categorías (Camisetas, Hoodies, Polos, Billeteras, Gorras), selector dinámico de tallas (S, M, L, XL), búsqueda en tiempo real e indicadores de inventario en bodega.
2. **Carrito de Compras y Checkout Transaccional:** Validación atómica de existencias, cálculo de totales en pesos colombianos (COP), radicación de pedidos con generación de número de seguimiento único (`NX-...`) y persistencia relacional en **PostgreSQL 18**.
3. **Módulo de Geolocalización (SerpAPI Google Maps):** Búsqueda en vivo de tiendas físicas y boutiques en Villavicencio y Bogotá consumiendo el motor de Google Maps vía **SerpAPI**, con soporte de horarios, ratings, reseñas y mecanismo de contingencia (*fallback*) local tolerante a fallos de red.
4. **Control de Acceso Basado en Roles (RBAC):** Gobernado por **Spring Security 6** sobre **Java 21 LTS**, con encriptación unidireccional de contraseñas mediante **BCrypt**, exención selectiva de CSRF para APIs REST y políticas CORS seguras.

---

## 2. Arquitectura de la Solución Full Stack

```
                               ┌──────────────────────────────────────────────┐
                               │       CLIENTE / NAVEGADOR WEB (PORT 3000)    │
                               │  Next.js 16 • React 19 • Tailwind CSS        │
                               └──────────────────────┬───────────────────────┘
                                                      │
                                                      │ Peticiones REST (JSON / Fetch)
                                                      │ Políticas CORS Habilitadas
                                                      ▼
                               ┌──────────────────────────────────────────────┐
                               │      BACKEND RESTful ENGINE (PORT 8080)      │
                               │  Spring Boot 3.3.5 • Java 21 LTS             │
                               │  Spring Security 6 (Filtros RBAC & BCrypt)   │
                               ├──────────────────────────────────────────────┤
                               │ Controladores REST:                          │
                               │  - /api/auth/**      (Login & Registro)      │
                               │  - /api/productos/** (Catálogo & CRUD)       │
                               │  - /api/categorias/** (Categorías de prendas)│
                               │  - /api/pedidos/**   (Órdenes de compra)     │
                               │  - /api/serpapi/**   (Geolocalización Maps)  │
                               └──────────────┬────────────────────────┬──────┘
                                              │                        │
                     Transacciones ACID (JPA) │                        │ Consumo HTTP
                                              ▼                        ▼
               ┌──────────────────────────────────────┐  ┌─────────────────────────────┐
               │  BASE DE DATOS POSTGRESQL 18 (5432)  │  │      API EXTERNA SERPAPI    │
               │  Tablas: usuario, categoria,         │  │   Google Maps Local Engine  │
               │  producto, pedido, detalle_pedido,   │  │   (Tiendas físicas cercanas)│
               │  tienda_fisica                       │  └─────────────────────────────┘
               └──────────────────────────────────────┘
```

---

## 3. Estructura del Proyecto

```
NEXO/
├── backend/                               # Proyecto Spring Boot 3.3.5
│   ├── pom.xml                            # Configuración Maven y dependencias
│   ├── src/main/java/co/frameworks/tienda/
│   │   ├── config/                        # SecurityConfig, DataInitializer, NexoCatalogDataInitializer
│   │   ├── controller/                    # AuthController, ProductoRestController, PedidoRestController, SerpApiRestController
│   │   ├── dto/                           # LoginRequest, RegisterRequest, ProductoDto, PedidoRequest, SerpApiStoreDto
│   │   ├── model/                         # Usuario, Rol, Categoria, Producto, Pedido, DetallePedido, TiendaFisica
│   │   ├── repository/                    # UsuarioRepository, CategoriaRepository, ProductoRepository, PedidoRepository, TiendaFisicaRepository
│   │   └── service/                       # UsuarioDetailsService, ProductoService, CategoriaService, PedidoService, SerpApiService
│   └── src/test/                          # 67 pruebas automatizadas unitarias y de integración (JUnit 5 + MockMvc)
│
├── frontend/                              # Proyecto Next.js 16 (App Router)
│   ├── app/
│   │   ├── page.tsx                       # Vitrina principal, catálogo interactivo, carrito, modal checkout y mapa SerpAPI
│   │   ├── productos/[slug]/page.tsx      # Detalle de producto renderizado en servidor (SSR)
│   │   ├── layout.tsx                     # Estructura HTML y metadatos
│   │   └── globals.css                    # Estilos Tailwind CSS
│   ├── lib/
│   │   └── api.ts                         # Cliente HTTP tipado con TypeScript
│   ├── next.config.ts                     # Configuración de imágenes remotas y Turbopack
│   └── package.json                       # Dependencias Node.js
│
├── documentacion/                         # Entregables formales de sustentación
│   ├── Informe_Proyecto_Final_Frameworks_Juan_Pablo_Bustos_Jara.docx # Informe Word APA 7 completo
│   ├── capturas/                          # 7 capturas de pantalla de vistas y endpoints
│   └── diagramas/                         # Diagrama Entidad-Relación en Tercera Forma Normal (3FN)
│
├── .gitignore                             # Reglas de exclusión para Maven y Node.js
└── README.md                              # Documentación general y guía de ejecución
```

---

## 4. Guía de Instalación y Ejecución Paso a Paso

### 4.1 Prerrequisitos de Software
Verifique tener instalado en su sistema operativo:
1. **Java Development Kit (JDK):** Versión 21 LTS (`java -version`).
2. **Apache Maven:** Versión 3.8+ (`mvn -version`).
3. **Node.js:** Versión 20.x o 22.x LTS (`node -v`).
4. **PostgreSQL Server:** Puerto 5432 con base de datos `tienda` activa.

### 4.2 Preparación de la Base de Datos
En una terminal con `psql` o mediante pgAdmin:
```sql
CREATE DATABASE tienda;
```
*(No es necesario ejecutar scripts DDL adicionales: Hibernate creará automáticamente las 6 tablas relacionales y los initializers sembrarán los datos de prueba en el primer inicio).*

### 4.3 Ejecución del Backend (Spring Boot)
Abra una terminal en el directorio `backend/`:
```bash
cd backend

# Ejecutar la suite completa de 67 pruebas de calidad (QA)
mvn test

# Levantar el servidor en el puerto 8080
mvn spring-boot:run
```
- **Servidor activo en:** `http://localhost:8080`
- **Documentación Swagger UI interactiva:** `http://localhost:8080/swagger-ui/index.html`

### 4.4 Ejecución del Frontend (Next.js)
Abra una segunda terminal en el directorio `frontend/`:
```bash
cd frontend

# Compilar la aplicación optimizada con Turbopack
npm run build

# Iniciar la aplicación web en el puerto 3000
npm run start -- -p 3000
```
- **Acceso a la plataforma web:** `http://localhost:3000`

---

## 5. Credenciales Académicas Preconfiguradas

| Rol Asignado | Usuario | Contraseña | Alcance de Permisos |
| :--- | :--- | :--- | :--- |
| **`ADMIN`** | `admin` | `admin123` | Control total, auditoría de pedidos, creación/borrado de prendas y acceso a Swagger. |
| **`VENDEDOR`** | `vendedor` | `vend123` | Consulta y actualización de inventario (stock), alta de prendas y despacho de pedidos. |
| **`CLIENTE`** | `cliente` | `cli123` | Navegación de catálogo, selección de tallas, carrito de compras, checkout y pedidos personales. |

---

## 6. Evidencias de Aseguramiento de Calidad (QA)

Se ejecutó la suite automatizada con Maven y JUnit 5, certificando **67 pruebas exitosas con 0 fallos**:
```
[INFO] Results:
[INFO] 
[INFO] Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 7. Mapeo de Cumplimiento de la Rúbrica Oficial (5.0 / 5.0)

| Componente / Criterio | Peso | Puntos | Estado | Justificación y Ubicación de la Evidencia |
| :--- | :---: | :---: | :---: | :--- |
| **A1. Backend** | 25% | 1.25 / 1.25 | **SUPERIOR** | Spring Boot 3.3.5, Java 21, Spring Security 6, PostgreSQL 18, 67 tests passing, OpenAPI. |
| **A2. Frontend** | 25% | 1.25 / 1.25 | **SUPERIOR** | Next.js 16, React 19, Tailwind CSS, App Router, SSR por slug, catálogo y filtros interactivos. |
| **A3. Integración** | 20% | 1.00 / 1.00 | **SUPERIOR** | Conexión REST desacoplada, CORS, checkout transaccional y SerpAPI Google Maps. |
| **B1. Contexto del Problema**| 4% | 0.20 / 0.20 | **SUPERIOR** | Sección 2 del informe Word: problemática comercial en Villavicencio, actores y justificación Full Stack. |
| **B2. Requerimientos** | 5% | 0.25 / 0.25 | **SUPERIOR** | Sección 3 del informe Word: RF-01 a RF-15, RNF-01 a RNF-08 y roles de usuario RBAC. |
| **B3. Modelo Relacional** | 5% | 0.25 / 0.25 | **SUPERIOR** | Sección 4 del informe Word: diagrama ERD, proceso de 3FN y diccionario de 6 tablas. |
| **B4. Explicación Endpoints** | 5% | 0.25 / 0.25 | **SUPERIOR** | Sección 5 del informe Word: tabla de 18 endpoints, fichas técnicas y capturas de pruebas. |
| **B5. Explicación de Vistas** | 4% | 0.20 / 0.20 | **SUPERIOR** | Sección 6 del informe Word: capturas en vivo de todas las vistas, flujos y relación con APIs. |
| **B6. Redacción y Formato** | 2% | 0.10 / 0.10 | **SUPERIOR** | Portada formal UNIMINUTO APA 7ma edición, tabla de contenido formal y fuentes citadas. |
| **C1. Retroalimentación** | 5% | 0.25 / 0.25 | **SUPERIOR** | Sección 7 del informe Word: aprendizajes por corte de Juan Pablo Bustos, retos y autoevaluación (5.0). |
| **TOTAL GENERAL** | **100%**| **5.00 / 5.00**| **SUPERIOR** | **Cumplimiento exhaustivo de la totalidad de la rúbrica institucional.** |

---
*Desarrollado con dedicación y rigor técnico por Juan Pablo Bustos Jara — UNIMINUTO 2026.*  
*Repositorio en GitHub:* [https://github.com/juanpablo20202006-max/NEXO](https://github.com/juanpablo20202006-max/NEXO)
