import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api-error.model';

/** Convierte cualquier error HTTP en un texto listo para mostrar al usuario. */
export function mensajeDeError(error: unknown, porDefecto = 'Ocurrió un error inesperado'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return porDefecto;
  }
  if (error.status === 0) {
    return 'No se pudo conectar con el servidor. Intenta nuevamente.';
  }
  if (error.status === 429) {
    return 'Demasiados intentos fallidos. Espera unos minutos antes de volver a intentarlo.';
  }
  const cuerpo = error.error as Partial<ApiError> | null;
  return cuerpo?.message || porDefecto;
}
