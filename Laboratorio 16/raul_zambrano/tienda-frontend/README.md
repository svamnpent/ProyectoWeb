# Laboratorio 16 — Comunicación frontend y backend

Extiende la Tienda UTP de la semana 15 con un recurso de diagnóstico que no toca el inventario.

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/diagnostico/estado` | 200 con JWT. 401 sin token. |
| POST | `/api/diagnostico/validar` | 200 si `nombre`, `monto` y `fecha` (`yyyy-MM-dd`) son válidos. |
| POST inválido | mismo | 400 `VALIDACION` o `JSON_INVALIDO`. |

`correo` es opcional. Si se envía, debe tener formato de email.

MySQL debe estar en `localhost:3306`, base `tienda_db`. La clave JWT es obligatoria, igual que en el laboratorio 10.

```bash
cd "Laboratorio 16/raul_zambrano/tienda"
export DB_PASSWORD='root'
export JWT_SECRET_BASE64="$(openssl rand -base64 32)"
bash ./mvnw spring-boot:run

cd "../tienda-frontend"
npm start
```

Entrar con `admin` / `Admin123*` o `usuario` / `Usuario123*` y abrir Diagnóstico. CORS permite solo `http://localhost:4200`.
