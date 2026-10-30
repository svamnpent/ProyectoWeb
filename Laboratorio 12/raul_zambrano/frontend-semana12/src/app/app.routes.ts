import { Routes } from '@angular/router';

import { Categorias } from './pages/categorias/categorias';
import { Inicio } from './pages/inicio/inicio';
import { NoEncontrado } from './pages/no-encontrado/no-encontrado';
import { ProductoDetalle } from './pages/producto-detalle/producto-detalle';
import { Productos } from './pages/productos/productos';

export const routes: Routes = [
  { path: '', component: Inicio },
  { path: 'productos', component: Productos },
  { path: 'productos/:id', component: ProductoDetalle },
  { path: 'categorias', component: Categorias },
  { path: '**', component: NoEncontrado },
];
