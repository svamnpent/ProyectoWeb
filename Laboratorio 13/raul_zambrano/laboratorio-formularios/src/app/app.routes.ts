import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'plantilla',
  },
  {
    path: 'plantilla',
    loadComponent: () =>
      import('./pages/formulario-plantilla/formulario-plantilla').then(
        (m) => m.FormularioPlantillaComponent,
      ),
  },
  {
    path: 'reactivo',
    loadComponent: () =>
      import('./pages/formulario-reactivo/formulario-reactivo').then(
        (m) => m.FormularioReactivoComponent,
      ),
  },
  {
    path: '**',
    redirectTo: 'plantilla',
  },
];
