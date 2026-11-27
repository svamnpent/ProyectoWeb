export interface Producto {
  id: number;
  nombre: string;
  categoria: string;
  precio: number;
  stock: number;
}

export type ProductoEntrada = Omit<Producto, 'id'>;

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
}

export interface Perfil {
  username: string;
  roles: string[];
}

export interface ResumenAdmin {
  totalProductos: number;
  productosConStockBajo: number;
  totalMovimientos: number;
  estado: string;
}
