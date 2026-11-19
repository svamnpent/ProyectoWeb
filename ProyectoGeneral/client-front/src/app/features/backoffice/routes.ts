import { Routes } from '@angular/router';
import { adminGuard } from '../../core/guards/auth.guard';
import { Panel } from './panel';

export const backofficeRoutes: Routes = [
  {
    path: '',
    component: Panel,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'productos' },
      { path: 'productos', loadComponent: () => import('./productos').then((m) => m.Productos) },
      { path: 'categorias', loadComponent: () => import('./categorias').then((m) => m.Categorias) },
      { path: 'etiquetas', loadComponent: () => import('./etiquetas').then((m) => m.Etiquetas) },
      { path: 'ventas', loadComponent: () => import('./ventas').then((m) => m.Ventas) },
      {
        path: 'auditoria',
        canActivate: [adminGuard],
        loadComponent: () => import('./auditoria/auditoria').then((m) => m.Auditoria),
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        loadComponent: () => import('./usuarios').then((m) => m.Usuarios),
      },
      {
        path: 'perfiles',
        canActivate: [adminGuard],
        loadComponent: () => import('./perfiles').then((m) => m.Perfiles),
      },
    ],
  },
];
