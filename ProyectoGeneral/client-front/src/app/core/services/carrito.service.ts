import { Injectable, computed, signal } from '@angular/core';
import { ItemCarrito } from '../models/carrito.model';
import { Producto } from '../models/catalogo.model';

const CLAVE_CARRITO = 'phantom.carrito';

/**
 * Carrito de compras. Se guarda en localStorage para sobrevivir a recargas.
 * Los datos guardados son una "foto" del producto; `sincronizar()` los pone al día
 * con el catálogo real (precio, stock, productos eliminados). El servidor es quien
 * decide el precio final y valida el stock al registrar la venta.
 */
@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly _items = signal<ItemCarrito[]>(this.leerGuardado());

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
    this.actualizar((items) =>
      existente
        ? items.map((i) => (i === existente ? { ...i, cantidad: i.cantidad + 1 } : i))
        : [...items, { producto, cantidad: 1 }],
    );
    return true;
  }

  /** Fija la cantidad de un producto. Devuelve false si no es válida (entera, ≥ 1 y ≤ stock). */
  establecerCantidad(productoId: number, cantidad: number): boolean {
    const item = this._items().find((i) => i.producto.id === productoId);
    if (!item || !Number.isInteger(cantidad) || cantidad < 1 || cantidad > item.producto.stock) {
      return false;
    }
    this.actualizar((items) => items.map((i) => (i === item ? { ...i, cantidad } : i)));
    return true;
  }

  quitar(productoId: number): void {
    this.actualizar((items) => items.filter((i) => i.producto.id !== productoId));
  }

  vaciar(): void {
    this.actualizar(() => []);
  }

  /**
   * Pone el carrito al día con el catálogo vigente: quita productos eliminados o agotados,
   * ajusta cantidades al stock y refresca precios. Devuelve los avisos para el usuario.
   */
  sincronizar(vigentes: Producto[]): string[] {
    const porId = new Map(vigentes.map((p) => [p.id, p]));
    const avisos: string[] = [];
    const resultado: ItemCarrito[] = [];

    for (const item of this._items()) {
      const actual = porId.get(item.producto.id);
      if (!actual) {
        avisos.push(`"${item.producto.nombre}" ya no está disponible y se quitó del carrito.`);
        continue;
      }
      if (actual.stock <= 0) {
        avisos.push(`"${actual.nombre}" se agotó y se quitó del carrito.`);
        continue;
      }
      let cantidad = item.cantidad;
      if (cantidad > actual.stock) {
        cantidad = actual.stock;
        avisos.push(`Se ajustó "${actual.nombre}" a ${cantidad} unidad(es) por falta de stock.`);
      }
      if (actual.precio !== item.producto.precio) {
        avisos.push(`El precio de "${actual.nombre}" cambió a S/ ${actual.precio.toFixed(2)}.`);
      }
      resultado.push({ producto: actual, cantidad });
    }

    this.actualizar(() => resultado);
    return avisos;
  }

  // ── Persistencia ───────────────────────────────────────────
  private actualizar(cambio: (items: ItemCarrito[]) => ItemCarrito[]): void {
    this._items.update(cambio);
    try {
      localStorage.setItem(CLAVE_CARRITO, JSON.stringify(this._items()));
    } catch {
      // Almacenamiento bloqueado: el carrito sigue funcionando mientras la pestaña esté abierta.
    }
  }

  private leerGuardado(): ItemCarrito[] {
    try {
      const datos: unknown = JSON.parse(localStorage.getItem(CLAVE_CARRITO) ?? '[]');
      return Array.isArray(datos) && datos.every(esItemValido) ? datos : [];
    } catch {
      return [];
    }
  }
}

/** El contenido de localStorage no es de fiar: se valida su forma antes de usarlo. */
function esItemValido(dato: unknown): dato is ItemCarrito {
  const item = dato as Partial<ItemCarrito> | null;
  const p = item?.producto as Partial<Producto> | undefined;
  return (
    typeof p?.id === 'number' &&
    typeof p.nombre === 'string' &&
    typeof p.precio === 'number' &&
    typeof p.stock === 'number' &&
    Number.isInteger(item?.cantidad) &&
    (item?.cantidad as number) >= 1
  );
}
