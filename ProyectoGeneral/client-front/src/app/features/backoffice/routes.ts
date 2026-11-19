import { Routes } from '@angular/router';
import { adminGuard } from '../../core/guards/auth.guard';
import { Panel } from './panel/panel';

export const backofficeRoutes: Routes = [
  {
    path: '',
    component: Panel,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'productos' },
      { path: 'productos', loadComponent: () => import('./catalogo/productos/productos').then((m) => m.Productos) },
      { path: 'categorias', loadComponent: () => import('./catalogo/categorias/categorias').then((m) => m.Categorias) },
      { path: 'etiquetas', loadComponent: () => import('./catalogo/etiquetas/etiquetas').then((m) => m.Etiquetas) },
      { path: 'ventas', loadComponent: () => import('./ventas/ventas').then((m) => m.Ventas) },
      {
        path: 'auditoria',
        canActivate: [adminGuard],
        loadComponent: () => import('./auditoria/auditoria').then((m) => m.Auditoria),
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        loadComponent: () => import('./cuentas/usuarios/usuarios').then((m) => m.Usuarios),
      },
      {
        path: 'perfiles',
        canActivate: [adminGuard],
        loadComponent: () => import('./cuentas/perfiles/perfiles').then((m) => m.Perfiles),
      },
    ],
  },
];
