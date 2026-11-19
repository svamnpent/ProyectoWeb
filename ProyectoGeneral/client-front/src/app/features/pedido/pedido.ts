import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { EMPTY, catchError, switchMap } from 'rxjs';
import { Venta } from '../../core/models/venta.model';
import { VentaService } from '../../core/services/venta.service';
import { mensajeDeError } from '../../core/utils/http-error';

@Component({
  selector: 'app-pedido',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './pedido.html',
  styleUrl: './pedido.scss',
})
export class Pedido {
  private readonly ventaService = inject(VentaService);

  /** Llega desde la ruta /pedido/:id gracias a withComponentInputBinding(). */
  readonly id = input.required<string>();

  protected readonly venta = signal<Venta | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    // Cada vez que cambia el :id de la ruta se vuelve a consultar el pedido.
    toObservable(this.id)
      .pipe(
        switchMap((id) => {
          this.cargando.set(true);
          this.error.set(null);
          this.venta.set(null);

          const numero = Number(id);
          if (!Number.isInteger(numero) || numero < 1) {
            this.error.set('El número de pedido no es válido.');
            this.cargando.set(false);
            return EMPTY;
          }
          return this.ventaService.obtener(numero).pipe(
            catchError((e) => {
              this.error.set(mensajeDeError(e, 'No se pudo cargar el pedido'));
              this.cargando.set(false);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe((venta) => {
        this.venta.set(venta);
        this.cargando.set(false);
      });
  }
}
