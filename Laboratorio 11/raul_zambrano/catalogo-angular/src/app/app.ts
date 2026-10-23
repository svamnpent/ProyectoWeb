import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SummaryPanel } from './features/summary-panel/summary-panel';
import { AppFooter } from './shared/app-footer/app-footer';
import { AppHeader } from './shared/app-header/app-header';

interface Producto {
  id: number;
  nombre: string;
  precio: number;
  stock: number;
}

@Component({
  selector: 'app-root',
  imports: [AppHeader, AppFooter, SummaryPanel, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly titulo = 'Catálogo de productos';
  protected readonly mostrarStock = signal(true);
  protected busqueda = '';

  protected readonly productos: Producto[] = [
    { id: 1, nombre: 'Laptop', precio: 3500, stock: 8 },
    { id: 2, nombre: 'Teclado mecánico', precio: 250, stock: 0 },
    { id: 3, nombre: 'Monitor 27 pulgadas', precio: 980, stock: 4 },
  ];

  protected alternarStock(): void {
    this.mostrarStock.update((valor) => !valor);
  }
}
