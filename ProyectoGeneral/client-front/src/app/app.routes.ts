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
    path: 'backoffice',
    canActivate: [staffGuard],
    canActivateChild: [staffGuard],
    loadChildren: () => import('./features/backoffice/routes').then((m) => m.backofficeRoutes),
  },

  { path: 'registro', loadComponent: () => import('./features/cliente/registro/registro').then(m => m.Registro) },
  { path: 'mis-pedidos', canActivate: [authGuard], loadComponent: () => import('./features/cliente/pedidos/pedidos').then(m => m.Pedidos) },
  { path: 'mi-cuenta', canActivate: [authGuard], loadComponent: () => import('./features/cliente/cuenta/cuenta').then(m => m.Cuenta) },
  { path: 'buscar', data: {tipo: 'buscar'}, loadComponent: () => import('./features/cliente/catalogo/explorar').then(m => m.Explorar) },
  { path: 'categoria/:id', data: {tipo: 'categoria'}, loadComponent: () => import('./features/cliente/catalogo/explorar').then(m => m.Explorar) },
  { path: 'etiqueta/:id', data: {tipo: 'etiqueta'}, loadComponent: () => import('./features/cliente/catalogo/explorar').then(m => m.Explorar) },

  { path: '**', redirectTo: '' },
];
