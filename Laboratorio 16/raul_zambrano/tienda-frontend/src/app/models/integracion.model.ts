export interface EstadoIntegracion {
  estado: string;
  servicio: string;
  fecha: string;
}

export interface SolicitudIntegracion {
  nombre: string;
  monto: number;
  fecha: string;
  correo?: string;
}

export interface RespuestaIntegracion extends SolicitudIntegracion {
  mensaje: string;
}

export interface ApiError {
  codigo: string;
  mensaje: string;
  fecha: string;
}
