import { HttpErrorResponse } from '@angular/common/http';

export function mensajeHttp(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado.';
  }
  if (error.status === 0) {
    return 'Sin respuesta de la API. Revise la red, CORS o que el servidor esté encendido.';
  }
  if (error.status === 401) {
    return 'Sesión ausente o JWT vencido.';
  }
  if (error.status === 403) {
    return 'No tiene permisos para esta operación.';
  }
  if (error.status === 404) {
    return 'Ruta o recurso no encontrado.';
  }
  const cuerpo = error.error as { message?: string; mensaje?: string } | null;
  if (typeof cuerpo?.message === 'string') {
    return cuerpo.message;
  }
  if (typeof cuerpo?.mensaje === 'string') {
    return cuerpo.mensaje;
  }
  return `Error HTTP ${error.status}.`;
}
