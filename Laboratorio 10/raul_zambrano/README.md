# Tienda API — Laboratorio 10 (APF2)

## 1. Objetivo

Integrar persistencia JPA, consultas JPQL, transacciones, roles y JWT en un back-end REST listo para el avance APF2. Esta semana no introduce una tecnología nueva: demuestra que las piezas de las semanas 6 a 9 funcionan juntas.

## 2. Arquitectura

```
Cliente / Postman
    Authorization: Bearer <JWT>
            │
            ▼
SecurityFilterChain + JwtDecoder
            │
            ▼
@RestController
            │
            ▼
@Service  (@Transactional y reglas de negocio)
            │
            ▼
JpaRepository / JPQL
            │
            ▼
MySQL (tienda_db)
```

Capas: `controller` → `service` → `repository` → `entity`.

## 3. Requisitos

- Java 21
- Maven Wrapper (`./mvnw`)
- MySQL 8 (`tienda_db`)
- Spring Boot 3.4.5

## 4. Variables de entorno

| Variable | Uso |
|---|---|
| `DB_PASSWORD` | Contraseña de MySQL (por defecto `root` si no se define) |
| `JWT_SECRET_BASE64` | Clave HMAC de al menos 32 bytes en Base64. Obligatoria al arrancar. |

No versionar secretos ni JWT reales.

```bash
export DB_PASSWORD='root'
export JWT_SECRET_BASE64="$(openssl rand -base64 32)"
```

## 5. Base de datos

Tablas: `productos`, `movimientos_stock`, `usuarios`, `roles`, `usuarios_roles`.

```bash
docker compose up -d
```

Al iniciar, `DatosIniciales` y `DatosSeguridadIniciales` cargan productos y los usuarios de laboratorio si aún no existen. Las contraseñas se guardan con BCrypt.

Consulta JPQL de evidencia:

```sql
-- Hibernate traduce esta JPQL:
SELECT p FROM Producto p
WHERE p.precio BETWEEN :min AND :max
ORDER BY p.precio ASC
```

## 6. Ejecución

```bash
cd "Laboratorio 10/raul_zambrano"
export JWT_SECRET_BASE64="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

API en `http://localhost:8080`.

## 7. Endpoints

| Método y ruta | Acceso |
|---|---|
| `POST /api/auth/login` | Público |
| `GET /api/auth/me` | JWT válido |
| `GET /api/publico/estado` | Público |
| `GET /api/productos` | USER o ADMIN |
| `GET /api/productos/rango?min=&max=` | USER o ADMIN (JPQL) |
| `POST /api/productos` | ADMIN |
| `PUT /api/productos/{id}` | ADMIN |
| `DELETE /api/productos/{id}` | ADMIN |
| `POST /api/inventario/{id}/salida?cantidad=` | ADMIN |
| `GET /api/admin/resumen` | ADMIN |

## 8. Usuarios y roles de prueba

| Username | Rol | Uso |
|---|---|---|
| `usuario` | `ROLE_USER` | Consultas |
| `admin` | `ROLE_ADMIN` + `ROLE_USER` | Escritura, inventario y resumen |

Contraseñas de laboratorio (solo aula): `Usuario123*` y `Admin123*`.

Login:

```json
{ "username": "usuario", "password": "Usuario123*" }
```

Respuesta: `{ "tokenType": "Bearer", "accessToken": "...", "expiresInSeconds": 1800 }`.

## 9. Pruebas

```bash
./mvnw clean test
./mvnw clean package
```

Matriz APF2:

| N.° | Escenario | Esperado |
|---|---|---|
| 1 | Login USER válido | 200 + accessToken |
| 2 | GET productos sin token | 401 |
| 3 | GET productos USER | 200 |
| 4 | POST productos USER | 403 |
| 5 | POST productos ADMIN | 201 |
| 6 | GET `/api/productos/rango` USER | 200 filtrado |
| 7 | POST salida válida ADMIN | 200 y baja stock |
| 8 | POST salida con stock insuficiente | 409 y rollback |
| 9 | GET resumen USER | 403 |
| 10 | GET resumen ADMIN | 200 |
| 11 | JWT alterado | 401 |
| 12 | Producto inexistente | 404 |

401 = no hay autenticación válida. 403 = autenticado sin permiso.

## 10. Evidencias de transacción y seguridad

Una salida de inventario (`registrarSalida`) es una sola transacción:

1. Busca el producto.
2. Valida cantidad y stock.
3. Descuenta stock (dirty checking de JPA).
4. Inserta `movimientos_stock` tipo `SALIDA`.
5. Commit.

Si el stock no alcanza se lanza `StockInsuficienteException` (HTTP 409) **antes** de persistir el movimiento: ni stock ni movimiento cambian.

El JWT no incluye la contraseña. Los roles viajan en el claim `roles` y Spring Security los convierte a `GrantedAuthority`.
