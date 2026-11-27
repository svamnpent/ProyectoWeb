import { Routes } from '@angular/router';
import { Usuarios } from './usuarios/usuarios';
import { UsuarioDetalle } from './usuarios/usuario-detalle';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'usuarios',
  },
  {
    path: 'usuarios',
    component: Usuarios,
  },
  {
    path: 'usuarios/:id',
    component: UsuarioDetalle,
  },
  {
    path: '**',
    redirectTo: 'usuarios',
  },
];
