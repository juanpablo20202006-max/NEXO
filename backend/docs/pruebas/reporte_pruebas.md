# Reporte de Pruebas Automatizadas (QA)
**Proyecto:** Tienda Online con Spring Boot 3.3.5 y Spring Security 6  
**Fecha:** 9 de Noviembre de 2026  
**Estado General:** ✅ **60 de 60 pruebas ejecutadas exitosamente (100% éxito, 0 errores, 0 fallos)**

---

## 1. Resumen Ejecutivo de Pruebas

| Suite de Pruebas | Clase de Test | Pruebas Ejecutadas | Errores | Fallos | Estado |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **Inicialización de Datos** | `DataInitializerTest` | 3 | 0 | 0 | ✅ Pasó |
| **Controlador Web (MVC)** | `PaginaControllerTest` | 23 | 0 | 0 | ✅ Pasó |
| *— Rutas Públicas* | `...$RutasPublicasTests` | 5 | 0 | 0 | ✅ Pasó |
| *— Rutas Protegidas Anónimo* | `...$RutasProtegidasAnonimoTests` | 3 | 0 | 0 | ✅ Pasó |
| *— Redirección Inteligente* | `...$RedireccionTests` | 5 | 0 | 0 | ✅ Pasó |
| *— Acceso por Roles* | `...$RutasPorRolTests` | 9 | 0 | 0 | ✅ Pasó |
| *— Cierre de Sesión* | `...$LogoutTests` | 1 | 0 | 0 | ✅ Pasó |
| **Entidad de Dominio** | `UsuarioTest` | 5 | 0 | 0 | ✅ Pasó |
| **Persistencia JPA** | `UsuarioRepositoryTest` | 4 | 0 | 0 | ✅ Pasó |
| **Seguridad y Filtros** | `SecurityConfigTest` | 19 | 0 | 0 | ✅ Pasó |
| *— Codificador BCrypt* | `...$PasswordEncoderTests` | 3 | 0 | 0 | ✅ Pasó |
| *— Rutas Públicas* | `...$PublicRoutesTests` | 3 | 0 | 0 | ✅ Pasó |
| *— Matriz de Roles* | `...$RoleBasedAccessTests` | 11 | 0 | 0 | ✅ Pasó |
| *— Logout y Token CSRF* | `...$LogoutAndCsrfTests` | 2 | 0 | 0 | ✅ Pasó |
| **Servicio de Usuarios** | `UsuarioDetailsServiceTest` | 5 | 0 | 0 | ✅ Pasó |
| **Contexto Spring Boot** | `TiendaApplicationTests` | 1 | 0 | 0 | ✅ Pasó |
| **TOTAL** | **7 Suites Principales** | **60** | **0** | **0** | **100% OK** |

---

## 2. Detalle de Cobertura por Capa

1. **Capa de Modelo y Dominio (`Usuario`, `Rol`):**
   - Validación de constructores completos y por defecto (JPA).
   - Verificación de asignación de roles (`ADMIN`, `VENDEDOR`, `CLIENTE`).
   - Métodos de igualdad, hashcode y representación en cadena.

2. **Capa de Persistencia (`UsuarioRepository`):**
   - Búsqueda por `username` mediante `findByUsername(String)`.
   - Verificación de existencia mediante `existsByUsername(String)`.
   - Garantía de restricciones de unicidad e integridad relacional en PostgreSQL.

3. **Capa de Seguridad (`SecurityConfig`, `UsuarioDetailsService`):**
   - Intercepción de rutas anónimas hacia `/login` con código HTTP 302.
   - Restricción de `/admin/**` exclusivamente a `ROLE_ADMIN`.
   - Acceso cruzado a `/vendedor/**` para `ROLE_VENDEDOR` y `ROLE_ADMIN`.
   - Acceso cruzado a `/cliente/**` para `ROLE_CLIENTE` y `ROLE_ADMIN`.
   - Acceso prohibido (HTTP 403 Forbidden) redirigido a `/acceso-denegado`.
   - Protección CSRF habilitada y validada en peticiones POST.
   - Cifrado unidireccional y verificación con `BCryptPasswordEncoder`.

4. **Capa de Controladores y Vistas (`PaginaController`):**
   - Despacho de vistas Thymeleaf (`index`, `login`, `admin`, `vendedor`, `cliente`, `acceso-denegado`).
   - Algoritmo de redirección jerárquica post-login en `/redirigir`:
     * `ADMIN` -> `/admin`
     * `VENDEDOR` -> `/vendedor`
     * `CLIENTE` -> `/cliente`
   - Flujo de cierre de sesión seguro vía POST `/logout` con invalidación de sesión HTTP y limpieza de cookies.
