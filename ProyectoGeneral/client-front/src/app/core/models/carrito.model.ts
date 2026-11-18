import { Producto } from './catalogo.model';

export interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}
