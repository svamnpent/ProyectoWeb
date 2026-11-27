import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

/** Registra método, ruta sin query y estado. No registra tokens ni cuerpos. */
export const diagnosticoErrorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (req.url.startsWith(environment.apiUrl)) {
        console.warn('API:', req.method, req.url.split('?')[0], 'status:', error.status);
      }
      return throwError(() => error);
    }),
  );
