import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ItemCarrito } from '../../core/models/carrito.model';
import { VentaRequest } from '../../core/models/venta.model';
import { AuthService } from '../../core/services/auth.service';
import { CarritoService } from '../../core/services/carrito.service';
import { ProductoService } from '../../core/services/producto.service';
import { VentaService } from '../../core/services/venta.service';
import { mensajeDeError } from '../../core/utils/http-error';

type LineaForm = FormGroup<{
  productoId: FormControl<number>;
  cantidad: FormControl<number>;
}>;

const entero = (control: AbstractControl): ValidationErrors | null =>
  Number.isInteger(control.value) ? null : { entero: true };

@Component({
  selector: 'app-carrito',
  imports: [ReactiveFormsModule, CurrencyPipe, RouterLink],
  templateUrl: './carrito.html',
  styleUrl: './carrito.scss',
})
export class Carrito {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly productoService = inject(ProductoService);
  private readonly ventaService = inject(VentaService);
  protected readonly carrito = inject(CarritoService);

  /** Un FormGroup por producto del carrito (controles dinámicos). */
  protected readonly formulario = this.fb.group({
    lineas: this.fb.array<LineaForm>([]),
  });

  protected readonly sincronizando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly avisos = signal<string[]>([]);
  /** Mensaje de validación de la cantidad, por id de producto. */
  protected readonly erroresLinea = signal<Record<number, string>>({});

  protected readonly etiquetaCompra = computed(() => {
    if (!this.auth.estaAutenticado()) {
      return 'Inicia sesión para comprar';
    }
    return this.enviando() ? 'Procesando…' : 'Confirmar compra';
  });

  constructor() {
    this.reconstruir();
    this.sincronizar();
  }

  protected get lineas(): FormArray<LineaForm> {
    return this.formulario.controls.lineas;
  }

  /** Suma o resta unidades desde los botones +/−. */
  protected cambiarPor(indice: number, delta: number): void {
    const control = this.lineas.at(indice).controls.cantidad;
    const nueva = control.value + delta;
    if (nueva >= 1) {
      control.setValue(nueva);
    }
  }

  protected quitar(indice: number): void {
    const productoId = this.lineas.at(indice).controls.productoId.value;
    this.carrito.quitar(productoId);
    this.lineas.removeAt(indice);
    this.limpiarError(productoId);
  }

  protected vaciar(): void {
    this.carrito.vaciar();
    this.reconstruir();
    this.error.set(null);
  }

  protected comprar(): void {
    if (this.enviando() || this.lineas.length === 0) {
      return;
    }
    if (this.formulario.invalid) {
      this.error.set('Corrige las cantidades marcadas antes de continuar.');
      return;
    }
    if (!this.auth.estaAutenticado()) {
      // El carrito vive en el navegador, así que se conserva durante el login.
      void this.router.navigate(['/login'], { queryParams: { returnUrl: '/carrito' } });
      return;
    }

    const solicitud: VentaRequest = {
      usuarioId: this.auth.usuario()?.id,
      detalles: this.lineas
        .getRawValue()
        .map(({ productoId, cantidad }) => ({ productoId, cantidad })),
    };

    this.enviando.set(true);
    this.error.set(null);

    this.ventaService
      .crear(solicitud)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (venta) => {
          this.carrito.vaciar();
          void this.router.navigate(['/pedido', venta.id]);
        },
        error: (e) => {
          this.error.set(mensajeDeError(e, 'No se pudo completar la compra'));
          // El stock pudo cambiar: se actualiza para que el usuario vea lo real.
          this.sincronizar();
        },
      });
  }

  // ── Privados ───────────────────────────────────────────────
  private sincronizar(): void {
    if (this.carrito.items().length === 0) {
      this.sincronizando.set(false);
      return;
    }
    this.sincronizando.set(true);

    this.productoService
      .listar()
      .pipe(finalize(() => this.sincronizando.set(false)))
      .subscribe({
        next: (productos) => {
          this.avisos.set(this.carrito.sincronizar(productos));
          this.reconstruir();
        },
        error: () =>
          this.avisos.set([
            'No se pudo verificar el stock actualizado; se muestran los últimos datos conocidos.',
          ]),
      });
  }

  /** Reconstruye el FormArray a partir del carrito (mismo orden que `carrito.items()`). */
  private reconstruir(): void {
    this.lineas.clear();
    this.erroresLinea.set({});
    for (const item of this.carrito.items()) {
      this.lineas.push(this.crearLinea(item));
    }
  }

  private crearLinea(item: ItemCarrito): LineaForm {
    const cantidad = this.fb.control(item.cantidad, [
      Validators.required,
      Validators.min(1),
      Validators.max(item.producto.stock),
      entero,
    ]);
    // Cada cambio válido se refleja en el carrito; los inválidos solo muestran el mensaje.
    cantidad.valueChanges.subscribe(() => this.alCambiarCantidad(item, cantidad));

    return this.fb.group({ productoId: this.fb.control(item.producto.id), cantidad });
  }

  private alCambiarCantidad(item: ItemCarrito, control: FormControl<number>): void {
    const id = item.producto.id;
    if (control.valid) {
      this.carrito.establecerCantidad(id, control.value);
      this.limpiarError(id);
      return;
    }
    const mensaje = control.hasError('max')
      ? `Solo hay ${item.producto.stock} unidades disponibles.`
      : 'Ingresa una cantidad entera de 1 o más.';
    this.erroresLinea.update((errores) => ({ ...errores, [id]: mensaje }));
  }

  private limpiarError(productoId: number): void {
    this.erroresLinea.update((errores) => {
      const { [productoId]: _quitado, ...resto } = errores;
      return resto;
    });
  }
}
