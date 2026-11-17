## ▶️ Cómo ejecutar el proyecto

### Requisitos

- **Docker Desktop** (levanta la base de datos y la API).
- **Node.js** `^20.19`, `^22.12` o `>=24` (incluye npm) para el frontend.
- Java y Maven solo si quieres ejecutar la API desde el IDE (ver `api/README.md`).

### 1. API y base de datos

```bash
cd api
cp .env.example .env          # en PowerShell: copy .env.example .env
```

Edita `.env` y revisa al menos:

- `DB_PASSWORD`, `JWT_SECRET` (mínimo 32 caracteres) y `ADMIN_PASSWORD`.
- `CORS_ALLOWED_ORIGINS` debe incluir `http://localhost:4200` (origen de Angular).

```bash
docker compose up -d --build
```

Comprueba que está arriba:

- Salud: <http://localhost:8080/actuator/health> → `{"status":"UP"}`
- Swagger: <http://localhost:8080/swagger-ui.html>

### 2. Frontend (Angular)

```bash
cd client-front
npm install
npm start
```

Abre <http://localhost:4200> (usa `localhost`, no `127.0.0.1`: CORS los trata como orígenes distintos).

### 3. Usuarios de prueba

Con `SPRING_PROFILES_ACTIVE=demo` en el `.env` se cargan datos falsos. Clave de todos: `Demo12345`.

| Rol | Correo |
|---|---|
| USER | `maria.quispe@demo.local`, `carlos.huaman@demo.local`, `lucia.torres@demo.local` |
| TRABAJADOR | `trabajador@demo.local` |
| ADMIN | el `ADMIN_EMAIL` / `ADMIN_PASSWORD` de tu `.env` |

### 4. Pruebas

```bash
cd client-front && npm test      # frontend (vitest)
cd api && ./mvnw test            # backend, no necesita Docker
```

### 5. Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| "No se pudo conectar con el servidor" en Angular | API apagada o CORS. Prueba <http://localhost:8080/actuator/health>; si responde `UP`, revisa `CORS_ALLOWED_ORIGINS`. |
| Cambié el `.env` y nada cambia | Usa `docker compose up -d api`, **no** `restart` (restart conserva las variables antiguas). |
| Quiero empezar con la BD limpia | `docker compose down -v` borra los datos y los vuelve a cargar al arrancar. |

> El archivo `.env` **no se sube a Git**. Si despliegas fuera de clase, cambia `JWT_SECRET` y `ADMIN_PASSWORD`, y quita el perfil `demo`.
