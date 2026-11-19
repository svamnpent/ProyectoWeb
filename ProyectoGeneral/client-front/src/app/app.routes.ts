import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'PHANTOM | Inicio',
    loadComponent: () => import('./features/inicio/inicio').then((m) => m.Inicio),
  },

  {
    path: 'login',
    title: 'PHANTOM | Iniciar sesión',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },

  {
    path: 'carrito',
    title: 'PHANTOM | Carrito',
    loadComponent: () => import('./features/carrito/carrito').then((m) => m.Carrito),
  },
  {
    // :id llega como input() al componente (withComponentInputBinding)
    path: 'pedido/:id',
    title: 'PHANTOM | Pedido',
    canActivate: [authGuard],
    loadComponent: () => import('./features/pedido/pedido').then((m) => m.Pedido),
  },
  {
    path: 'producto/:id',
    title: 'PHANTOM | Detalle de Producto',
    loadComponent: () => import('./features/producto/producto').then((m) => m.ProductoComponent),
  },

  // Rutas previstas (se agregan al construir cada pantalla):
  //   { path: 'registro', ... }                                → formulario reactivo
  //   { path: 'producto/:id', ... }                            → :id llega como input()
  //   { path: 'categoria/:id', ... }
  //   { path: 'perfil', canActivate: [authGuard], ... }
  //   { path: 'admin/productos', canActivate: [staffGuard], ... }

  { path: '**', redirectTo: '' },
];
