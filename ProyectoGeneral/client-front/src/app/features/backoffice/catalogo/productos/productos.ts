import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import {
  Categoria,
  Etiqueta,
  Producto,
  ProductoRequest,
} from '../../../../core/models/catalogo.model';
import { Mantenimiento } from '../../compartido/operaciones';

@Component({
  imports: [FormsModule],
  templateUrl: './productos.html',
  styleUrl: '../../compartido/pagina.scss',
})
export class Productos extends Mantenimiento<ProductoRequest, Producto> {
  protected readonly porPagina = 5;
  private paginaElegida = 1;

  protected get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.filas.length / this.porPagina));
  }

  protected get paginaActual(): number {
    return Math.min(this.paginaElegida, this.totalPaginas);
  }

  protected get productosVisibles(): Producto[] {
    const inicio = (this.paginaActual - 1) * this.porPagina;
    return this.filas.slice(inicio, inicio + this.porPagina);
  }

  protected cambiarPagina(paso: number): void {
    this.paginaElegida = Math.max(1, Math.min(this.totalPaginas, this.paginaActual + paso));
  }

  protected readonly recurso = 'productos';
  protected categorias: Categoria[] = [];
  protected etiquetas: Etiqueta[] = [];
  ngOnInit() {
    void this.ejecutar(() => this.cargar());
  }
  protected nuevo(): ProductoRequest {
    return {
      nombre: '',
      stock: 0,
      precio: 0,
      categoriaId: this.categorias[0]?.id ?? 0,
      etiquetaIds: [],
    };
  }
  protected copiar(fila: Producto): ProductoRequest {
    return {
      nombre: fila.nombre,
      descripcion: fila.descripcion ?? undefined,
      precio: fila.precio,
      stock: fila.stock,
      marca: fila.marca ?? undefined,
      imagenUrl: fila.imagenUrl ?? undefined,
      categoriaId: fila.categoria.id,
      etiquetaIds: fila.etiquetas.map((e) => e.id),
    };
  }
  protected async cargar() {
    [this.categorias, this.etiquetas] = await Promise.all([
      firstValueFrom(this.http.get<Categoria[]>(this.api + '/categorias')),
      firstValueFrom(this.http.get<Etiqueta[]>(this.api + '/etiquetas')),
    ]);
    this.filas = await firstValueFrom(this.http.get<Producto[]>(this.api + '/productos'));
  }
  protected recargar() {
    return this.ejecutar(() => this.cargar());
  }
}
