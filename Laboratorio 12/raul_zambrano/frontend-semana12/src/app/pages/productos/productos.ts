import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { ProductoCard } from '../../components/producto-card/producto-card';
import { PRODUCTOS } from '../../data/productos.mock';

@Component({
  selector: 'app-productos',
  imports: [ProductoCard],
  templateUrl: './productos.html',
  styleUrl: './productos.scss',
})
export class Productos {
  private readonly router = inject(Router);

  readonly productos = signal(PRODUCTOS);
  readonly favoritos = signal(0);
  readonly comparaciones = signal<number[]>([]);

  irDetalle(id: number): void {
    this.router.navigate(['/productos', id]);
  }

  registrarFavorito(id: number): void {
    console.log('Favorito recibido desde el hijo:', id);
    this.favoritos.update((valor) => valor + 1);
  }

  registrarComparacion(id: number): void {
    this.comparaciones.update((ids) => (ids.includes(id) ? ids : [...ids, id]));
  }
}
