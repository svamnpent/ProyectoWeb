import { Injectable, computed, signal } from '@angular/core';
import { ItemCarrito } from '../models/carrito.model';
import { Producto } from '../models/catalogo.model';

/**
 * Carrito en memoria (se pierde al recargar). Cuando se construya la pantalla
 * del carrito se podrá persistir y enviar al backend con POST /ventas.
 */
@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly _items = signal<ItemCarrito[]>([]);

  readonly items = this._items.asReadonly();
  readonly cantidadTotal = computed(() => this._items().reduce((s, i) => s + i.cantidad, 0));
  readonly total = computed(() =>
    this._items().reduce((s, i) => s + i.producto.precio * i.cantidad, 0),
  );

  /** Agrega una unidad. Devuelve false si ya se alcanzó el stock disponible. */
  agregar(producto: Producto): boolean {
    const existente = this._items().find((i) => i.producto.id === producto.id);
    if ((existente?.cantidad ?? 0) >= producto.stock) {
      return false;
    }
    this._items.update((items) =>
      existente
        ? items.map((i) => (i === existente ? { ...i, cantidad: i.cantidad + 1 } : i))
        : [...items, { producto, cantidad: 1 }],
    );
    return true;
  }

  vaciar(): void {
    this._items.set([]);
  }
}
