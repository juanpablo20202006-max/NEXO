# Tienda Online - Sistema de Gestión Web con Seguridad por Roles

Aplicación web desarrollada como proyecto académico para la asignatura de **Frameworks Web**, implementando arquitectura basada en capas con **Spring Boot 3.3.5**, control de acceso granular mediante **Spring Security 6**, persistencia relacional con **Spring Data JPA** sobre **PostgreSQL**, y renderizado dinámico del lado del servidor con **Thymeleaf**.

---

## 1. Arquitectura y Tecnologías del Proyecto

- **Lenguaje:** Java 21 LTS (Oracle JDK / OpenJDK compatible con Project Loom).
- **Framework Web:** Spring Boot 3.3.5 (LTS moderna sobre Jakarta EE 10).
- **Módulo de Seguridad:** Spring Security 6 (configuración funcional vía `SecurityFilterChain`).
- **Motor de Plantillas:** Thymeleaf 3 + `thymeleaf-extras-springsecurity6`.
- **Capa de Persistencia:** Spring Data JPA / Hibernate 6.
- **Motor de Base de Datos:** PostgreSQL 15+ (probado y validado en PostgreSQL 18 local).
- **Herramienta de Construcción:** Apache Maven 3.9+.

```
tienda/
├── docs/
│   ├── capturas/                  # 9 capturas de pantalla reales en Edge
│   ├── pruebas/                   # Reportes de QA y suites automatizadas
│   ├── analisis_compatibilidad.md # Justificación de Java 21 y Spring Boot 3.3.5
│   └── PREGUNTAS_SUSTENTACION.md  # 5 preguntas clave con respuestas para sustentación
├── src/
│   ├── main/
│   │   ├── java/co/frameworks/tienda/
│   │   │   ├── config/            # SecurityConfig, DataInitializer
│   │   │   ├── controller/        # PaginaController (@Controller)
│   │   │   ├── model/             # Entidad Usuario, enum Rol
│   │   │   ├── repository/        # UsuarioRepository (Spring Data JPA)
│   │   │   ├── service/           # UsuarioDetailsService
│   │   │   └── TiendaApplication.java
│   │   └── resources/
│   │       ├── static/css/        # estilos.css (Diseño moderno y responsivo)
│   │       ├── templates/         # Plantillas Thymeleaf (index, login, etc.)
│   │       │   └── fragments/     # navbar.html reutilizable
│   │       └── application.properties
│   └── test/                      # 60 pruebas unitarias y de integración
├── .env.example
├── pom.xml
└── README.md
```

---

## 2. Prerrequisitos

Asegúrate de contar con las siguientes herramientas instaladas en el sistema:

1. **Java Development Kit (JDK):** Versión 21 LTS (`java -version`).
2. **Apache Maven:** Versión 3.8+ (`mvn -version`).
3. **PostgreSQL Server:** Puerto 5432 activo con base de datos creada (`tienda`).

---

## 3. Configuración de Base de Datos y Variables de Entorno

La aplicación lee los parámetros de conexión desde variables de entorno o recurre a los valores predeterminados configurados en `application.properties`:

| Variable de Entorno | Valor Predeterminado | Descripción |
| :--- | :--- | :--- |
| `DB_USERNAME` | `postgres` | Usuario administrador de PostgreSQL |
| `DB_PASSWORD` | `1234` | Contraseña del usuario de PostgreSQL |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/tienda` | URL JDBC de la base de datos |

### Creación de la Base de Datos en PostgreSQL:
```sql
CREATE DATABASE tienda;
```

*(Hibernate generará automáticamente la tabla `usuario` y sus restricciones en el primer arranque mediante `spring.jpa.hibernate.ddl-auto=update`).*

---

## 4. Credenciales Académicas de Acceso

Al iniciar la aplicación, `DataInitializer` verifica la base de datos y crea automáticamente los siguientes usuarios con contraseñas codificadas en **BCrypt**:

| Rol | Nombre de Usuario | Contraseña | Rutas Autorizadas |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` | `/admin/**`, `/vendedor/**`, `/cliente/**`, `/` |
| **Vendedor** | `vendedor` | `vend123` | `/vendedor/**`, `/` |
| **Cliente** | `cliente` | `cli123` | `/cliente/**`, `/` |

---

## 5. Matriz de Autorización y Rutas

| Ruta HTTP | Permisos Requeridos | Comportamiento si no está autorizado |
| :--- | :--- | :--- |
| `/` | Público (`permitAll`) | Acceso libre para cualquier usuario |
| `/login` | Público (`permitAll`) | Muestra formulario de inicio de sesión con CSRF |
| `/acceso-denegado` | Público (`permitAll`) | Muestra mensaje de error HTTP 403 amigable |
| `/css/**`, `/favicon.ico` | Público (`permitAll`) | Carga de recursos estáticos |
| `/admin/**` | Exclusivo `ROLE_ADMIN` | Redirige a `/acceso-denegado` si está autenticado; a `/login` si es anónimo |
| `/vendedor/**` | `ROLE_VENDEDOR` o `ROLE_ADMIN` | Redirige a `/acceso-denegado` si es Cliente; a `/login` si es anónimo |
| `/cliente/**` | `ROLE_CLIENTE` o `ROLE_ADMIN` | Redirige a `/acceso-denegado` si es Vendedor; a `/login` si es anónimo |
| `/redirigir` | Autenticado (`authenticated`) | Evalúa roles y redirige al panel según jerarquía |
| `/logout` | Autenticado (POST) | Invalida sesión, borra cookies y redirige a `/` |

---

## 6. Instrucciones de Compilación y Ejecución

### Ejecución de Pruebas Automatizadas (60 tests):
```powershell
mvn clean test
```

### Empaquetado del Archivo JAR:
```powershell
mvn clean package
```

### Ejecutar con Maven en Desarrollo:
```powershell
mvn spring-boot:run
```

### Ejecutar el Archivo JAR Generado:
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="1234"
java -jar target/tienda-1.0.0.jar
```

La aplicación quedará disponible en: **`http://localhost:8080/`**

---

## 7. Guía de Solución de Problemas Frecuentes

1. **Error de conexión a la base de datos (`Connection to localhost:5432 refused`):**
   - Verifica que el servicio de PostgreSQL esté en ejecución en Windows Services (`services.msc`) o con `net start postgresql-x64-18`.
   - Asegúrate de que la base de datos `tienda` haya sido creada.
2. **Error de credenciales de PostgreSQL (`FATAL: password authentication failed`):**
   - Define la variable de entorno `$env:DB_PASSWORD = "tu_password"` antes de ejecutar la aplicación.
3. **Puerto 8080 en uso (`Port 8080 was already in use`):**
   - Identifica y finaliza el proceso que ocupa el puerto:
     ```powershell
     Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force
     ```
4. **Error 403 Forbidden al hacer POST en `/logout`:**
   - Spring Security exige token CSRF. En Thymeleaf, el atributo `th:action="@{/logout}"` inyecta automáticamente el token CSRF dentro del formulario.
