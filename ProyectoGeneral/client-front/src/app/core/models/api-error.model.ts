// Formato de error que devuelve el GlobalExceptionHandler del backend.
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string; // en errores de validación vienen concatenados
  path: string;
}
