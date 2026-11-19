import { UsuarioResumen } from './usuario.model';

export interface DetalleVenta {
  id: number;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Venta {
  id: number;
  fecha: string; // ISO-8601 con offset (-05:00)
  total: number;
  cantidadItems: number;
  numeroOrden: string | null;
  usuario: UsuarioResumen | null;
  detalles: DetalleVenta[];
}

export interface DetalleVentaRequest {
  productoId: number;
  cantidad: number; // mínimo 1
}

export interface VentaRequest {
  /**
   * El backend ignora este valor para compradores (siempre compran a su nombre).
   * Para TRABAJADOR/ADMIN es obligatorio enviarlo; si no, la venta queda anónima.
   */
  usuarioId?: number;
  detalles: DetalleVentaRequest[];
}
