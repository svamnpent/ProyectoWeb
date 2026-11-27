import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { mensajeHttp } from '../../core/http-error';
import { ProductoService } from '../../core/producto.service';
import { Producto } from '../../models/modelos';

@Component({
  selector: 'app-salida-stock',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './salida-stock.html',
  styleUrl: './salida-stock.css',
})
export class SalidaStock {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ProductoService);
  private readonly fb = inject(FormBuilder);

  readonly producto = signal<Producto | null>(null);
  readonly cargando = signal(true);
  readonly procesando = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    cantidad: [1, [Validators.required, Validators.min(1)]],
  });

  private readonly id = Number(this.route.snapshot.paramMap.get('id'));

  constructor() {
    if (!Number.isSafeInteger(this.id) || this.id <= 0) {
      this.error.set('El identificador de producto es inválido.');
      this.cargando.set(false);
      return;
    }
    this.api.listar().subscribe({
      next: (datos) => {
        const encontrado = datos.find((p) => p.id === this.id) ?? null;
        this.producto.set(encontrado);
        if (!encontrado) {
          this.error.set('Producto no encontrado.');
        }
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeHttp(e));
        this.cargando.set(false);
      },
    });
  }

  registrar(): void {
    const p = this.producto();
    this.form.markAllAsTouched();
    if (!p || this.form.invalid || this.procesando()) {
      return;
    }
    const cantidad = this.form.controls.cantidad.value;
    if (!Number.isInteger(cantidad) || cantidad > p.stock) {
      this.error.set('La cantidad debe ser entera y no superar el stock.');
      return;
    }
    this.procesando.set(true);
    this.error.set('');
    this.api.registrarSalida(p.id, cantidad).subscribe({
      next: () => void this.router.navigateByUrl('/productos'),
      error: (e) => {
        this.error.set(mensajeHttp(e));
        this.procesando.set(false);
      },
    });
  }
}
