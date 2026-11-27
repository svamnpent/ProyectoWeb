import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionService } from './sesion.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  return sesion.autenticado()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const adminGuard: CanActivateFn = (_route, state) => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  if (!sesion.autenticado()) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  return sesion.esAdmin() ? true : router.createUrlTree(['/productos']);
};
