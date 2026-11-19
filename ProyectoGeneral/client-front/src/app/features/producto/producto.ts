import { CurrencyPipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { EMPTY, catchError, switchMap } from 'rxjs';
import { Producto } from '../../core/models/catalogo.model';
import { CarritoService } from '../../core/services/carrito.service';
import { ProductoService } from '../../core/services/producto.service';
import { mensajeDeError } from '../../core/utils/http-error';

interface Aviso {
  tipo: 'exito' | 'error';
  texto: string;
}

@Component({
  selector: 'app-producto',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './producto.html',
  styleUrl: './producto.scss',
})
export class ProductoComponent {
  private readonly productoService = inject(ProductoService);
  private readonly carrito = inject(CarritoService);

  /** Llega desde la ruta /producto/:id gracias a withComponentInputBinding(). */
  readonly id = input.required<string>();

  protected readonly producto = signal<Producto | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly aviso = signal<Aviso | null>(null);

  protected readonly agotado = computed(() => (this.producto()?.stock ?? 0) <= 0);
  protected readonly avisoStock = computed(() => {
    const stock = this.producto()?.stock ?? 0;
    if (stock <= 0) {
      return 'Sin stock';
    }
    return stock <= 5 ? `¡Últimas ${stock} unidades disponibles!` : `En stock (${stock} unidades)`;
  });
  protected readonly inicial = computed(() => this.producto()?.nombre.charAt(0).toUpperCase() ?? '');

  private temporizadorAviso?: ReturnType<typeof setTimeout>;

  constructor() {
    inject(DestroyRef).onDestroy(() => clearTimeout(this.temporizadorAviso));

    toObservable(this.id)
      .pipe(
        switchMap((id) => {
          this.cargando.set(true);
          this.error.set(null);
          this.producto.set(null);

          const numero = Number(id);
          if (!Number.isInteger(numero) || numero < 1) {
            this.error.set('El identificador del producto no es válido.');
            this.cargando.set(false);
            return EMPTY;
          }

          return this.productoService.obtener(numero).pipe(
            catchError((e) => {
              this.error.set(mensajeDeError(e, 'No se pudo cargar el producto'));
              this.cargando.set(false);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe((prod) => {
        this.producto.set(prod);
        this.cargando.set(false);
      });
  }

  protected agregarAlCarrito(): void {
    const prod = this.producto();
    if (!prod) {
      return;
    }
    if (this.carrito.agregar(prod)) {
      this.mostrarAviso({ tipo: 'exito', texto: `"${prod.nombre}" se agregó al carrito` });
    } else {
      this.mostrarAviso({
        tipo: 'error',
        texto: `No puedes agregar más unidades. Límite de stock alcanzado.`,
      });
    }
  }

  private mostrarAviso(aviso: Aviso): void {
    clearTimeout(this.temporizadorAviso);
    this.aviso.set(aviso);
    this.temporizadorAviso = setTimeout(() => this.aviso.set(null), 3000);
  }
}
