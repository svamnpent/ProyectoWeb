# Laboratorio 11 — Fundamentos de Angular, componentes y SCSS

Catálogo local **sin consumo del API**. Rutas, formularios complejos y `HttpClient` quedan para semanas posteriores.

## Requisitos

- Node.js 22.22.3 o compatible con Angular 22
- npm
- Angular CLI 22

En esta máquina: `nvm use` (el `.nvmrc` fija `22.22.3`).

```bash
cd catalogo-angular
nvm use
npm start
```

Abre `http://localhost:4200/` (no escribas GET delante de la URL).

Build de evidencia:

```bash
cd catalogo-angular
npm run build
```

## Qué demuestra

- Componente raíz `App` con datos en memoria (`Laptop`, `Teclado mecánico`, `Monitor 27 pulgadas`)
- Interpolación, event binding, class binding, `@if` y `@for`
- Componentes extra: `app-header`, `app-footer`, `summary-panel`
- SCSS con variables, anidamiento, mixin y `@use` (`src/styles/_tokens.scss`)
- NgModel opcional en el buscador (solo muestra el texto; no llama al back-end)
