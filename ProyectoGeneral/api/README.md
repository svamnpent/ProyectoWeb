# Phantom Store API

API REST (Java 21, Spring Boot 3.3, PostgreSQL, Flyway) con seguridad JWT, caché local y tests.

## Arranque rápido (Docker)

```bash
cp .env.example .env        # edita DB_PASSWORD, JWT_SECRET (>=32 chars), ADMIN_EMAIL, ADMIN_PASSWORD
docker compose up -d --build
# Swagger UI: http://localhost:8080/swagger-ui.html   |   Health: http://localhost:8080/actuator/health
```

Solo la BD (API desde el IDE): `docker compose up -d db` y exporta `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, `JWT_SECRET`. Perfil con SQL visible: `SPRING_PROFILES_ACTIVE=local`.

Si ya tienes tu propio contenedor de Postgres, no necesitas el servicio `db` del compose:
las variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` siguen siendo las mismas de siempre.

## Datos de prueba, caché precargada y zona horaria

* **Datos demo** (perfil `demo`, activo por defecto en `.env.example`): 5 categorías, 6 etiquetas, 16 productos
  (uno agotado), 4 usuarios (3 compradores y 1 trabajador), 2 perfiles y 5 ventas. Solo se cargan si la BD no tiene categorías.
  Compradores: `maria.quispe@demo.local`, `carlos.huaman@demo.local`, `lucia.torres@demo.local`;
  personal: `trabajador@demo.local` (rol TRABAJADOR); clave de todos: `Demo12345`. El ADMIN es el de `ADMIN_EMAIL`.
  **Quita `SPRING_PROFILES_ACTIVE=demo` en producción.** Para recargarlos desde cero: `docker compose down -v`.
* **Caché precargada** al arrancar (categorías, etiquetas, productos). Desactivar: `APP_CACHE_WARMUP_ENABLED=false`.
* **Zona horaria: America/Lima (UTC-5).** JVM, Postgres y Hibernate la usan; en BD las fechas son `timestamptz`
  (instantes absolutos) y la API las devuelve con desfase, p. ej. `2026-10-13T20:47:56-05:00`.

## Seguridad: dos zonas y tres roles

Login en `POST /api/v1/auth/login` → `{accessToken, tokenType, expiresIn}` → cabecera `Authorization: Bearer <token>`.
La respuesta no incluye datos del usuario; para eso está `GET /api/v1/auth/me` (el rol también viaja en el JWT, claim `roles`).

Roles: **USER** (comprador), **TRABAJADOR** (personal con acceso limitado) y **ADMIN**.
La API se divide en dos zonas; la zona de back-office está en **una sola clase**, `BackOfficeController`,
que además documenta (en su Javadoc) y aplica quién puede cada operación.

### Zona TIENDA (`/api/v1/...`): lo que hace el comprador

| Endpoint | Anónimo | Comprador |
|---|---|---|
| `GET /productos`, `/categorias`, `/etiquetas` (+ `/{id}`, `/buscar`) | sí | sí |
| `POST /auth/register`, `POST /auth/login` | sí | sí |
| `GET /auth/me` | no | sí |
| `GET/PUT /usuarios/{id}` | no | solo la propia cuenta |
| `/perfiles` (crear, ver, editar, borrar) | no | solo el propio |
| `POST /ventas` (comprar, siempre a su nombre), `GET /ventas/mias` | no | sí |
| `GET /ventas/{id}`, `GET /ventas/{id}/detalles` | no | solo las propias |

### Zona BACK-OFFICE (`/api/v1/backoffice/...`): solo personal

| Operación | TRABAJADOR | ADMIN |
|---|---|---|
| Productos, categorías, etiquetas: crear y editar | sí | sí |
| Productos, categorías, etiquetas: eliminar | no | sí |
| Ventas: listar, ver, ver líneas | sí | sí |
| Ventas: registrar venta de mostrador (`POST /ventas`, con `usuarioId` opcional) | sí | sí |
| Ventas: anular (repone stock) | no | sí |
| Líneas de venta: agregar / quitar | no | sí |
| Usuarios (listar, buscar, crear, editar, cambiar rol, eliminar) y perfiles | no | sí |

Un comprador recibe 403 en toda la zona back-office y un anónimo 401. La escritura del catálogo ya no existe
en la zona tienda (responde 405, incluso para un ADMIN). Para cambiar un permiso, edita la constante
(`PERSONAL` o `SOLO_ADMIN`) de la anotación del método en `BackOfficeController`.

El cliente dentro de una venta se muestra solo con `id`, `nombre` y `email` (`UsuarioResumenDTO`): el personal
que atiende ventas no recibe DNI, dirección ni teléfono. Las fechas de la API (datos y errores) llevan el desfase
de Perú (`-05:00`).

El personal se da de alta con `POST /api/v1/backoffice/usuarios` (rol `TRABAJADOR`). Los roles solo los puede
cambiar un ADMIN; el cambio se nota al volver a iniciar sesión (los tokens ya emitidos conservan el rol hasta
que expiran).

## Tests

```bash
./mvnw test      # unitarios + slices web + caché local. NO necesita Docker
./mvnw verify    # además los *IT con PostgreSQL REAL (Testcontainers). Requiere Docker en marcha
```

* `*Test`: servicios (Mockito), seguridad, matriz de permisos de todos los endpoints, `CacheBehaviorTest`.
* `*IT`: arrancan la app completa contra `postgres:16-alpine` real: migraciones Flyway, validación
  de esquema de Hibernate, flujos de auth, ventas (rollback, concurrencia sin sobreventa) y caché.

## Caché local (Caffeine)

Cachés `categorias`, `etiquetas`, `productos` (10 min, máx. 1000 entradas; ver `spring.cache.caffeine.spec`).
Las escrituras invalidan lo necesario (p. ej. renombrar una categoría o comprar un producto invalida `productos`).
La caché es local a cada instancia: con varias réplicas hay que usar Redis u otra caché distribuida.
