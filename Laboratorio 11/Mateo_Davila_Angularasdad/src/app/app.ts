import { Component, signal } from '@angular/core';
import { AppHeader } from './shared/app-header/app-header';
interface Producto {
  id: number;
  nombre: string;
  precio: number;
  stock: number;
}
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AppHeader],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly titulo = 'Catálogo de productos';
  protected readonly mostrarStock = signal(true);
  protected readonly productos: Producto[] = [
    { id: 1, nombre: 'Laptop', precio: 3500, stock: 8 },
    { id: 2, nombre: 'Teclado mecánico', precio: 250, stock: 0 },
    { id: 3, nombre: 'Monitor 27 pulgadas', precio: 980, stock: 4 }
  ];
  protected alternarStock(): void {
    this.mostrarStock.update(valor => !valor);
  }
}
