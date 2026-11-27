# Laboratorio 15 — Integración Angular + Spring Boot

Frontend de Tienda UTP. Consume la API de `../tienda` (copia del laboratorio 10, con CORS para `http://localhost:4200`).

Contrato usado:

| Método | Ruta | Rol |
| --- | --- | --- |
| POST | `/api/auth/login` | Público |
| GET | `/api/auth/me` | JWT |
| GET | `/api/productos` y `/api/productos/rango` | USER o ADMIN |
| POST | `/api/productos` | ADMIN |
| POST | `/api/inventario/{id}/salida?cantidad=` | ADMIN |
| GET | `/api/admin/resumen` | ADMIN |

MySQL debe estar en `localhost:3306`, base `tienda_db`. La clave JWT es obligatoria, igual que en el laboratorio 10.

```bash
cd "Laboratorio 15/raul_zambrano/tienda"
export DB_PASSWORD='root'
export JWT_SECRET_BASE64="$(openssl rand -base64 32)"
bash ./mvnw spring-boot:run

cd "../tienda-frontend"
npm start
```

Cuentas de laboratorio: `admin` / `Admin123*` y `usuario` / `Usuario123*`.

El JWT queda en memoria. Un guard de Angular no reemplaza la autorización del backend.
