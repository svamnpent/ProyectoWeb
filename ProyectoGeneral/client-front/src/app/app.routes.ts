import { Routes } from '@angular/router';
import { authGuard, staffGuard } from './core/guards/auth.guard';

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
    path: 'registro',
    title: 'PHANTOM | Crear cuenta',
    loadComponent: () => import('./features/registro/registro').then((m) => m.Registro),
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
  {
    path: 'perfil',
    title: 'PHANTOM | Mi perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./features/perfil/perfil').then((m) => m.PerfilUsuario),
  },

  {
    path: 'backoffice',
    canActivate: [staffGuard],
    canActivateChild: [staffGuard],
    loadChildren: () => import('./features/backoffice/routes').then((m) => m.backofficeRoutes),
  },

  // Rutas previstas (se agregan al construir cada pantalla):
  //   { path: 'categoria/:id', ... }

  { path: '**', redirectTo: '' },
];
