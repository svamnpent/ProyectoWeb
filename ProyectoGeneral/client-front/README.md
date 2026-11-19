# PHANTOM — Frontend (Angular)

Tienda gamer que consume la API REST de `../api`. Angular 21 con componentes standalone, signals, control flow (`@if` / `@for`) y SCSS.

## Requisitos

- Node.js `^20.19`, `^22.12` o `>=24`
- La API corriendo en `http://localhost:8080` (ver el README de la raíz)

## Comandos

| Comando | Qué hace |
|---|---|
| `npm install` | Instala dependencias |
| `npm start` | Servidor de desarrollo en <http://localhost:4200> |
| `npm test` | Pruebas unitarias (vitest). Usa `npm test -- --watch=false` para una sola corrida |
| `npm run build` | Compilación de producción en `dist/` |

La URL de la API se define en `src/environments/` (`environment.development.ts` para `ng serve`, `environment.ts` para producción).

## Estructura

```text
src/
├── styles.scss          # Estilos globales (importa los parciales)
├── styles/              # Parciales SCSS: _variables, _mixins, _base, _botones, _formularios, _alertas
├── environments/        # apiUrl por entorno
└── app/
    ├── app.*            # Shell: header, footer, <router-outlet>, config y rutas
    ├── core/            # Lo que usa toda la app
    │   ├── models/      # Interfaces TypeScript que reflejan los DTOs del backend
    │   ├── services/    # AuthService, ProductoService, CategoriaService, VentaService, CarritoService
    │   ├── interceptors/# auth (agrega el Bearer) y error (401 → login)
    │   ├── guards/      # authGuard, rolGuard, staffGuard, adminGuard
    │   └── utils/       # mensajeDeError()
    ├── shared/          # Componentes reutilizables (producto-card)
    └── features/        # Una carpeta por pantalla (inicio, login, carrito, pedido)
```

## Rutas

| Ruta | Pantalla | Acceso |
|---|---|---|
| `/` | Catálogo con filtro por categoría y búsqueda | Público |
| `/login` | Inicio de sesión (formulario template-driven) | Público |
| `/carrito` | Carrito: cantidades (FormArray), total y compra | Público; para comprar pide iniciar sesión |
| `/pedido/:id` | Comprobante de la compra (`:id` llega como `input()`) | Con sesión (`authGuard`) |

Cualquier otra ruta redirige a `/`.

## Autenticación

- `AuthService.login()` llama a `POST /auth/login` y luego a `GET /auth/me` para conocer el rol.
- El token se guarda en `localStorage` (`phantom.token`) junto con su vencimiento y se restaura al abrir la app.
- `authInterceptor` agrega `Authorization: Bearer <token>` **solo** a peticiones hacia `environment.apiUrl`.
- `errorInterceptor` cierra la sesión y manda a `/login?returnUrl=...` si la API responde 401 con sesión activa.
- Los guards (`authGuard`, `staffGuard`, `adminGuard`) solo mejoran la experiencia: la seguridad real está en el backend.
- El login responde 429 tras 5 intentos fallidos (bloqueo de 15 minutos).

## Carrito y compra

- `CarritoService` guarda el carrito en `localStorage` (`phantom.carrito`) y valida su forma al leerlo.
- Al abrir `/carrito` se consulta `GET /productos` y se **sincroniza**: se quitan productos eliminados o agotados, se ajustan cantidades al stock y se refrescan precios, avisando al usuario.
- Cada línea es un `FormGroup` dentro de un `FormArray`; la cantidad se valida (entera, ≥ 1 y ≤ stock).
- Comprar envía `POST /ventas` con `{ usuarioId, detalles: [{ productoId, cantidad }] }`. El **precio lo decide el servidor**.
- `usuarioId` se envía siempre: para TRABAJADOR/ADMIN es obligatorio, o la venta queda anónima (los compradores normales siempre compran a su nombre).
- Si no hay stock suficiente la API responde 400 con el detalle; se muestra y se vuelve a sincronizar el carrito.
- Sin sesión, "Comprar" lleva a `/login?returnUrl=/carrito` y el carrito se conserva.
- Tras comprar, el carrito se vacía y se navega a `/pedido/:id`.

## Convenciones

- Componentes standalone con `inject()`; estado con `signal()` / `computed()`.
- Entradas y salidas con `input()` / `input.required()` / `output()`.
- Un componente = `nombre.ts`, `nombre.html`, `nombre.scss`, `nombre.spec.ts` (se generan con `ng generate component ruta/nombre`).
- SCSS: en cada archivo `@use 'variables' as *;` y `@use 'mixins' as *;`. Clases en español con BEM (`.tarjeta__precio`, `.btn--acento`).
- Cada servicio y componente nuevo lleva su `*.spec.ts` junto al archivo.
- Formato con Prettier (`npx prettier --write "src/**/*.{ts,html,scss}"`).

## Cómo agregar una pantalla

1. `npx ng generate component features/registro/registro`
2. Registrar la ruta en `app.routes.ts` con `loadComponent`, un `title` y, si hace falta, `canActivate`.
3. Los parámetros de ruta y de query llegan como `input()` gracias a `withComponentInputBinding()` (ver `Login.returnUrl`).
4. Consumir la API desde un servicio en `core/services/` y escribir su prueba con `HttpTestingController`.

## Pendiente

- Registro (formulario reactivo) y `/producto/:id` (detalle).
- Perfil e historial (`GET /ventas/mias`); desde el historial se puede enlazar a `/pedido/:id`.
- Panel de administración (CRUD en `/backoffice/**`), con `staffGuard` y `adminGuard`.
