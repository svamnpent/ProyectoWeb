import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/auth.guards';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'productos' },
  {
    path: 'login',
    title: 'Tienda UTP | Ingreso',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },
  {
    path: 'productos',
    title: 'Tienda UTP | Catálogo',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/productos/productos').then((m) => m.Productos),
  },
  {
    path: 'productos/nuevo',
    title: 'Tienda UTP | Nuevo producto',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/producto-nuevo/producto-nuevo').then((m) => m.ProductoNuevo),
  },
  {
    path: 'inventario/salida/:id',
    title: 'Tienda UTP | Salida de stock',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/salida-stock/salida-stock').then((m) => m.SalidaStock),
  },
  {
    path: 'diagnostico',
    title: 'Tienda UTP | Diagnóstico',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/diagnostico/diagnostico').then((m) => m.Diagnostico),
  },
  {
    path: 'admin/resumen',
    title: 'Tienda UTP | Resumen',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/admin-resumen/admin-resumen').then((m) => m.AdminResumen),
  },
  { path: '**', redirectTo: 'productos' },
];
