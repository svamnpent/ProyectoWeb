import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { PRODUCTOS } from '../../data/productos.mock';

@Component({
  selector: 'app-categorias',
  imports: [RouterLink],
  templateUrl: './categorias.html',
  styleUrl: './categorias.scss',
})
export class Categorias {
  readonly categorias = [...new Set(PRODUCTOS.map((producto) => producto.categoria))].map(
    (nombre) => ({
      nombre,
      cantidad: PRODUCTOS.filter((producto) => producto.categoria === nombre).length,
    }),
  );
}
