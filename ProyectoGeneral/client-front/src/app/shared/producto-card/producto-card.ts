import { CurrencyPipe } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { Producto } from '../../core/models/catalogo.model';

@Component({
  selector: 'app-producto-card',
  imports: [CurrencyPipe],
  templateUrl: './producto-card.html',
  styleUrl: './producto-card.scss',
})
export class ProductoCard {
  /** Producto a mostrar (obligatorio). */
  readonly producto = input.required<Producto>();
  /** Se emite cuando el usuario pulsa "Agregar al carrito". */
  readonly agregar = output<Producto>();

  protected readonly agotado = computed(() => this.producto().stock <= 0);
  protected readonly avisoStock = computed(() => {
    const stock = this.producto().stock;
    if (stock <= 0) {
      return 'Sin stock';
    }
    return stock <= 5 ? `Últimas ${stock} unidades` : null;
  });
  protected readonly inicial = computed(() => this.producto().nombre.charAt(0).toUpperCase());

  protected alAgregar(): void {
    this.agregar.emit(this.producto());
  }
}
