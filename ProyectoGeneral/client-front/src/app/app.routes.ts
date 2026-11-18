import { Routes } from '@angular/router';

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

  // Rutas previstas (se agregan al construir cada pantalla):
  //   { path: 'registro', ... }                                → formulario reactivo
  //   { path: 'producto/:id', ... }                            → :id llega como input()
  //   { path: 'categoria/:id', ... }
  //   { path: 'carrito', ... }                                 → FormArray de líneas
  //   { path: 'perfil', canActivate: [authGuard], ... }
  //   { path: 'admin/productos', canActivate: [staffGuard], ... }

  { path: '**', redirectTo: '' },
];
