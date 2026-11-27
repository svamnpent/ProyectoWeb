# Laboratorio 14 — Servicios y consumo de APIs

Aplicación `laboratorio-servicios-api`. El componente de usuarios no llama a `HttpClient`: las operaciones GET, POST, PUT, PATCH y DELETE viven en `UsuarioService` y consumen JSONPlaceholder.

```bash
cd "Laboratorio 14/raul_zambrano/laboratorio-servicios-api"
npm start
```

Abrir http://localhost:4200/usuarios.

JSONPlaceholder no persiste las escrituras: al recargar, la lista vuelve al estado original.

Complementos incluidos: búsqueda local, ruta `/usuarios/:id`, contador de solicitudes exitosas, botón PATCH, `apiUrl` en `environment.ts`, interceptor de registro y prueba del servicio (`npm test`).
