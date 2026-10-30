# Laboratorio 12 — Rutas en Angular y comunicación entre componentes

SPA **sin consumo del API**. Formularios complejos y `HttpClient` quedan para las semanas 13 y 14.

## Requisitos

- Node.js 22.22.3 o compatible con Angular 22
- npm
- Angular CLI 22

En esta máquina: `nvm use` (el `.nvmrc` fija `22.22.3`).

```bash
cd frontend-semana12
nvm use
npm start
```

Abre `http://localhost:4200/` (no escribas GET delante de la URL).

Build de evidencia:

```bash
cd frontend-semana12
npm run build
```

## Qué demuestra

- Angular Router con `app.routes.ts` y `provideRouter(routes)`
- Layout raíz con `RouterLink`, `RouterLinkActive` y `RouterOutlet`
- Rutas: `/`, `/productos`, `/productos/:id`, `/categorias` y comodín `**`
- Datos locales en `src/app/data/productos.mock.ts` (Laptop Pro, Monitor 27 pulgadas, Teclado mecánico)
- Precios con `currency:'PEN'` y locale `es-PE` (se ven como `S/`)
- Comunicación padre → hijo con `input.required<Producto>()` y `destacado`
- Comunicación hijo → padre con `output()`: `verDetalle`, `favorito` y `agregarComparacion`
- Navegación programática con `Router.navigate(['/productos', id])`
- Parámetro de ruta con `ActivatedRoute.snapshot.paramMap`

## Matriz de pruebas

| Prueba | Acción | Resultado esperado |
| --- | --- | --- |
| Inicio | Abrir `/` | Página Inicio dentro de `router-outlet` |
| Navegación | Pulsar Productos | Cambia a `/productos` sin recargar |
| Ruta activa | Observar menú | El enlace activo tiene clase `active` |
| Input | Ver tarjetas | Cada `ProductoCard` muestra el objeto del padre |
| Destacado | Precios mayores a S/ 1,000 | Laptop Pro y Monitor llevan clase especial |
| Output favorito | Pulsar Favorito dos veces | El padre incrementa el contador a 2 |
| Comparación | Pulsar Comparar | El padre muestra cuántos productos únicos se añadieron |
| Output + Router | Ver detalle en id 2 | Navega a `/productos/2` |
| Parámetro | Abrir `/productos/3` | Muestra Teclado mecánico |
| ID inexistente | Abrir `/productos/999` | Muestra Producto no encontrado |
| Wildcard | Abrir `/xyz` | Muestra la página 404 |
| Historial | Usar atrás/adelante | La navegación responde al historial |

## Reto D — consulta directa

`/productos/1` y `/productos/999` usan la misma ruta `productos/:id` y el mismo componente `ProductoDetalle`. Cambia el resultado porque `ActivatedRoute` entrega un `id` distinto: `1` encuentra Laptop Pro en el mock y `999` no existe, así que se muestra el estado de producto no encontrado.
