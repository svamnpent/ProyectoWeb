export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string | null;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion?: string;
}

export interface Etiqueta {
  id: number;
  nombre: string;
}

export interface EtiquetaRequest {
  nombre: string;
}

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string | null;
  precio: number;
  stock: number;
  marca: string | null;
  imagenUrl: string | null;
  categoria: Categoria;
  etiquetas: Etiqueta[];
}

export interface ProductoRequest {
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  marca?: string;
  imagenUrl?: string;
  categoriaId: number;
  etiquetaIds?: number[];
}
