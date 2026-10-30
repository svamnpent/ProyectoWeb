import { CurrencyPipe } from '@angular/common';
import { Component, input, output } from '@angular/core';

import { Producto } from '../../models/producto';

@Component({
  selector: 'app-producto-card',
  imports: [CurrencyPipe],
  templateUrl: './producto-card.html',
  styleUrl: './producto-card.scss',
})
export class ProductoCard {
  readonly producto = input.required<Producto>();
  readonly destacado = input(false);
  readonly verDetalle = output<number>();
  readonly favorito = output<number>();
  readonly agregarComparacion = output<number>();

  solicitarDetalle(): void {
    this.verDetalle.emit(this.producto().id);
  }

  marcarFavorito(): void {
    this.favorito.emit(this.producto().id);
  }

  solicitarComparacion(): void {
    this.agregarComparacion.emit(this.producto().id);
  }
}
