export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: string;
  dni?: string;
  direccion?: string;
  numeroTelefono?: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  usuario: Usuario;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegistroRequest {
  nombre: string;
  email: string;
  password: string;
  dni?: string;
  direccion?: string;
  numeroTelefono?: string;
}

export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string;
}

export interface Etiqueta {
  id: number;
  nombre: string;
}

export interface Producto {
  id: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  marca?: string;
  imagenUrl?: string;
  categoria: Categoria;
  etiquetas?: Etiqueta[];
}

export interface DetalleVentaRequest {
  productoId: number;
  cantidad: number;
}

export interface VentaRequest {
  usuarioId?: number;
  detalles: DetalleVentaRequest[];
}

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
  fecha: string;
  total: number;
  cantidadItems: number;
  numeroOrden: string;
  usuario?: Usuario;
  detalles: DetalleVenta[];
}

