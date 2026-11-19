import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/rol';
import { AuthService } from '../services/auth.service';

/** Exige sesión iniciada; si no, redirige a /login conservando la URL pedida. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.estaAutenticado()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/**
 * Exige sesión y uno de los roles indicados. Es solo experiencia de usuario:
 * la seguridad real la aplica el backend con @PreAuthorize.
 */
export const rolGuard =
  (...rolesPermitidos: Rol[]): CanActivateFn =>
  (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.estaAutenticado()) {
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }
    const rol = auth.rol();
    return rol !== null && rolesPermitidos.includes(rol) ? true : router.createUrlTree(['/']);
  };

export const staffGuard = rolGuard('ADMIN', 'TRABAJADOR');
export const adminGuard = rolGuard('ADMIN');
