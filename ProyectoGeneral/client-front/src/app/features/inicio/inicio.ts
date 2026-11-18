import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { Categoria, Producto } from '../../core/models/catalogo.model';
import { CarritoService } from '../../core/services/carrito.service';
import { CategoriaService } from '../../core/services/categoria.service';
import { ProductoService } from '../../core/services/producto.service';
import { mensajeDeError } from '../../core/utils/http-error';
import { ProductoCard } from '../../shared/producto-card/producto-card';

interface Aviso {
  tipo: 'exito' | 'error';
  texto: string;
}

@Component({
  selector: 'app-inicio',
  imports: [ProductoCard],
  templateUrl: './inicio.html',
  styleUrl: './inicio.scss',
})
export class Inicio {
  private readonly productoService = inject(ProductoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly carrito = inject(CarritoService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly aviso = signal<Aviso | null>(null);

  protected readonly categoriaSeleccionada = signal<number | null>(null);
  protected readonly busqueda = signal('');

  /** Filtra en el cliente por categoría y por texto (nombre o marca). */
  protected readonly productosFiltrados = computed(() => {
    const categoriaId = this.categoriaSeleccionada();
    const texto = this.busqueda().trim().toLowerCase();

    return this.productos().filter((p) => {
      const coincideCategoria = categoriaId === null || p.categoria.id === categoriaId;
      const coincideTexto =
        texto === '' ||
        p.nombre.toLowerCase().includes(texto) ||
        (p.marca ?? '').toLowerCase().includes(texto);
      return coincideCategoria && coincideTexto;
    });
  });

  private temporizadorAviso?: ReturnType<typeof setTimeout>;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.temporizadorAviso));
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    forkJoin({
      productos: this.productoService.listar(),
      categorias: this.categoriaService.listar(),
    }).subscribe({
      next: ({ productos, categorias }) => {
        this.productos.set(productos);
        this.categorias.set(categorias);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e, 'No se pudo cargar el catálogo'));
        this.cargando.set(false);
      },
    });
  }

  protected seleccionarCategoria(id: number | null): void {
    this.categoriaSeleccionada.set(id);
  }

  protected alBuscar(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected agregarAlCarrito(producto: Producto): void {
    if (this.carrito.agregar(producto)) {
      this.mostrarAviso({ tipo: 'exito', texto: `${producto.nombre} se agregó al carrito` });
    } else {
      this.mostrarAviso({
        tipo: 'error',
        texto: `No hay más unidades disponibles de ${producto.nombre}`,
      });
    }
  }

  private mostrarAviso(aviso: Aviso): void {
    clearTimeout(this.temporizadorAviso);
    this.aviso.set(aviso);
    this.temporizadorAviso = setTimeout(() => this.aviso.set(null), 3000);
  }
}
