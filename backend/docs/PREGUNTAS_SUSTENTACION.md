# Preguntas Clave para la Sustentación Técnica del Proyecto Tienda Online
**Frameworks Web: Spring Boot 3.3.5 / Spring Security 6 / Java 21 LTS / PostgreSQL**

Este documento recopila las cinco preguntas fundamentales para la sustentación oral del proyecto, con explicaciones conceptuales, técnicas y referencias directas al código implementado.

---

### 1. ¿Por qué se utilizó Spring Security 6 y Spring Boot 3.3.5 en lugar de versiones como 4.1.2?

#### Explicación Técnica y Justificación de Arquitectura:
- **Estado Real del Ecosistema Spring:**
  A la fecha de desarrollo, **Spring Boot 3.3.5** representa la versión estable LTS recomendada en producción por el equipo de Spring (Pivotal/VMware/Broadcom). Versiones de numeración superior como una hipotética 4.1.2 son versiones inexistentes en el ecosistema actual de Spring Boot y carecen de paquetes publicados en Maven Central.
- **Transición a Jakarta EE 10:**
  Spring Boot 3 y Spring Security 6 completaron la migración definitiva del namespace clásico de Java EE (`javax.servlet.*`) hacia **Jakarta EE 10** (`jakarta.servlet.*`, `jakarta.persistence.*`). Utilizar una versión consistente garantiza compatibilidad nativa sin capas de compatibilidad legado.
- **Modernización de la API de Seguridad:**
  En Spring Security 6 se eliminó por completo la clase obsoleta `WebSecurityConfigurerAdapter`. Toda la seguridad se configura mediante Beans basados en el patrón de diseño funcional `SecurityFilterChain` y lambdas, ofreciendo mayor inmutabilidad y legibilidad arquitectónica:
  ```java
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/", "/login", "/acceso-denegado", "/css/**").permitAll()
              .requestMatchers("/admin/**").hasRole("ADMIN")
              .requestMatchers("/vendedor/**").hasAnyRole("VENDEDOR", "ADMIN")
              .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")
              .anyRequest().authenticated()
          )
          ...
  }
  ```
- **Soporte Oficial para Java 21 LTS:**
  Spring Boot 3.3.5 cuenta con compatibilidad certificada con **Java 21 LTS**, aprovechando mejoras de rendimiento de la JVM, optimizaciones del recolector de basura ZGC/G1, y soporte nativo para el ecosistema de *Virtual Threads* (Project Loom).

---

### 2. ¿Qué diferencia existe entre autenticación y autorización?

#### Diferenciación Conceptual y Aplicación en el Proyecto:

| Criterio | Autenticación (Authentication) | Autorización (Authorization) |
| :--- | :--- | :--- |
| **Pregunta fundamental** | *¿Quién eres tú?* | *¿Qué tienes permiso de hacer o ver?* |
| **Propósito** | Validar la identidad declarada por el usuario. | Controlar el acceso a recursos específicos según roles o privilegios. |
| **Mecanismo en el proyecto** | Formulario `/login` con `username` y `password`. | Reglas de filtrado HTTP en `SecurityConfig` (`requestMatchers`) y directivas `sec:authorize`. |
| **Componentes Spring** | `UsuarioDetailsService`, `AuthenticationManager`, `BCryptPasswordEncoder`. | `SecurityFilterChain`, `AuthorizationManager`, `AccessDeniedHandler`. |
| **Resultado exitoso** | Creación de un token `Authentication` en el `SecurityContext`. | Acceso concedido al recurso solicitado (ej. `/admin`). |
| **Resultado fallido** | BadCredentialsException / Redirección a `/login?error`. | AccessDeniedException (HTTP 403) / Redirección a `/acceso-denegado`. |

#### Flujo en el Sistema:
1. El usuario envía sus credenciales al endpoint POST `/login`. **(Autenticación)**
2. `UsuarioDetailsService` busca el usuario en PostgreSQL y `BCryptPasswordEncoder` verifica la clave.
3. Si la clave es válida, el usuario queda autenticado y posee el rol asignado (`ROLE_ADMIN`, `ROLE_VENDEDOR` o `ROLE_CLIENTE`).
4. Cuando el usuario intenta navegar a `/admin`, el filtro de seguridad evalúa si su rol contiene `ADMIN`. **(Autorización)**

---

### 3. ¿Por qué no se deben guardar contraseñas en texto plano y cómo ayuda BCrypt?

#### Los Peligros del Texto Plano:
- Si un atacante compromete la base de datos mediante inyección SQL (SQLi), fuga de copias de seguridad o accesos indebidos, obtendrá inmediatamente todas las claves de los usuarios.
- Los usuarios suelen reutilizar contraseñas en múltiples plataformas (correo, banca, trabajo), por lo que una fuga expone toda la identidad digital del usuario.

#### Cómo Protege BCrypt:
1. **Función Hash Criptográfica Unidireccional (One-Way):**
   BCrypt aplica una transformación matemática irreversible. No existe un algoritmo para "desencriptar" el hash y recuperar la clave original; únicamente se puede verificar calculando el hash del texto ingresado y comparándolo con el almacenado.
2. **Generación Automática de Sal (Salt) Criptográfica:**
   BCrypt genera aleatoriamente un valor de 16 bytes (128 bits) llamado *Salt* que se incorpora al inicio del hash:
   `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
   - `$2a$`: Identificador del algoritmo BCrypt.
   - `$10$`: Factor de costo logarítmico ($2^{10} = 1024$ rondas de clave).
   - Los siguientes 22 caracteres corresponden a la sal aleatoria.
   - Los caracteres finales son el valor hash cifrado.
   *Efecto de la sal:* Aunque dos usuarios utilicen la misma clave (ej. `"123456"`), sus hashes en la base de datos serán completamente distintos. Esto inutiliza los ataques basados en Tablas Arcoíris (*Rainbow Tables*).
3. **Factor de Costo Adaptable (Slow Hash):**
   A diferencia de algoritmos diseñados para velocidad de datos como MD5 o SHA-256 (que permiten calcular miles de millones de hashes por segundo en tarjetas gráficas), BCrypt fue diseñado deliberadamente para ser lento y exigir memoria/CPU mediante el cifrador *Eksblowfish*. Si en el futuro el hardware se vuelve más potente, el factor de costo puede elevarse a 12 o 14 sin alterar la lógica de negocio.

---

### 4. ¿Cómo se relaciona el archivo de configuración de seguridad con los métodos del controlador?

#### Arquitectura de Intercepción Previa:
La relación se fundamenta en el patrón arquitectónico de **Filtros Interceptores en Cadena (FilterChain)**:

```
[ Petición HTTP ] 
       │
       ▼
[ SecurityFilterChain (SecurityConfig) ]  <--- Evalúa Autenticación y Reglas de Rol
       │
   ¿Autorizado?
       ├── NO  ──► Redirección a /acceso-denegado (HTTP 403)
       └── SÍ
            │
            ▼
   [ DispatcherServlet ]
            │
            ▼
   [ PaginaController (@GetMapping) ]    <--- Ejecuta la lógica y retorna la vista HTML
```

#### Reglas de Coherencia entre Configuración y Controlador:
- **`SecurityConfig` protege los patrones de URL:**
  Define qué roles pueden ingresar a una ruta. Por ejemplo:
  ```java
  .requestMatchers("/admin/**").hasRole("ADMIN")
  .requestMatchers("/vendedor/**").hasAnyRole("VENDEDOR", "ADMIN")
  .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")
  ```
- **`PaginaController` gestiona los endpoints y vistas:**
  Define qué vista renderizar una vez que la petición supera la seguridad:
  ```java
  @GetMapping("/admin")
  public String admin() {
      return "admin"; // Despacha templates/admin.html
  }
  ```
- **Intercepción Absoluta:**
  Si un usuario con rol `ROLE_CLIENTE` intenta acceder directamente a `GET /admin`, el filtro de Spring Security detiene la petición inmediatamente. El método `admin()` de `PaginaController` **nunca llega a ejecutarse**, garantizando que la lógica de negocio esté blindada ante accesos no autorizados.

---

### 5. ¿Por qué no es suficiente proteger una aplicación ocultando enlaces en la vista Thymeleaf?

#### La Falacia de la Seguridad por Oscuridad:
En la vista `navbar.html` se emplean directivas de Thymeleaf Extras:
```html
<li sec:authorize="hasRole('ADMIN')">
    <a th:href="@{/admin}">⚙️ Admin</a>
</li>
```
Ocultar este enlace en el navegador es una buena práctica de **Diseño y Experiencia de Usuario (UX)**, pues evita confundir a clientes con opciones administrativas que no pueden operar. Sin embargo, **no constituye una barrera de seguridad efectiva**.

#### Vulnerabilidad del Lado del Cliente:
- **Manipulación del DOM:** Un usuario malintencionado puede abrir las herramientas para desarrolladores de Edge o Chrome (F12) y alterar el HTML local para hacer visible cualquier botón.
- **Peticiones HTTP Directas:** Nada impide a un atacante escribir directamente `http://localhost:8080/admin` en la barra de direcciones de su navegador, o enviar una solicitud `GET` mediante herramientas de automatización como cURL, Postman o scripts de Node.js/Python.

#### Principio de Defensa en Profundidad (Defense-in-Depth):
- La seguridad debe residir **obligatoriamente en el servidor (backend)**.
- Thymeleaf adapta la interfaz visual según la sesión activa, pero es `SecurityFilterChain` en Spring Security quien valida rigurosamente los tokens de autorización en cada petición antes de despachar cualquier recurso sensible o ejecutar transacciones en la base de datos PostgreSQL.
