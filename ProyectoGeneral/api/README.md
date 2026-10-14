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

## Seguridad

Login en `POST /api/v1/auth/login` → `accessToken` → cabecera `Authorization: Bearer <token>`.

| Recurso | Anónimo | USER | ADMIN |
|---|---|---|---|
| `GET` productos / categorías / etiquetas | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE` catálogo | ❌ 401 | ❌ 403 | ✅ |
| `/auth/register`, `/auth/login` | ✅ | ✅ | ✅ |
| `/usuarios` (listar, buscar, crear, borrar) | ❌ | ❌ | ✅ |
| `/usuarios/{id}` GET/PUT | ❌ | solo el suyo | ✅ |
| `/perfiles` (listar) | ❌ | ❌ | ✅ |
| `/perfiles/{id}`, crear | ❌ | solo el suyo | ✅ |
| `POST /ventas`, `GET /ventas/mias` | ❌ | ✅ (a su nombre) | ✅ |
| `GET /ventas/{id}` y `/ventas/{id}/detalles` | ❌ | solo las suyas | ✅ |
| `GET /ventas`, `DELETE /ventas/{id}`, `POST/DELETE` detalles | ❌ | ❌ | ✅ |

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
