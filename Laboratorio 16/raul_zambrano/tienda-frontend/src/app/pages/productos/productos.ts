import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeHttp } from '../../core/http-error';
import { ProductoService } from '../../core/producto.service';
import { SesionService } from '../../core/sesion.service';
import { Producto } from '../../models/modelos';

@Component({
  selector: 'app-productos',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './productos.html',
  styleUrl: './productos.css',
})
export class Productos implements OnInit {
  private readonly api = inject(ProductoService);
  private readonly fb = inject(FormBuilder);
  protected readonly sesion = inject(SesionService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly filtro = this.fb.nonNullable.group({
    min: [0, [Validators.required, Validators.min(0)]],
    max: [1000, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.api.listar().subscribe({
      next: (datos) => {
        this.productos.set(datos);
        this.cargando.set(false);
      },
      error: (error) => {
        this.error.set(mensajeHttp(error));
        this.cargando.set(false);
      },
    });
  }

  protected filtrar(): void {
    if (this.filtro.invalid) {
      this.filtro.markAllAsTouched();
      return;
    }
    const { min, max } = this.filtro.getRawValue();
    if (min > max) {
      this.error.set('El mínimo no puede superar al máximo.');
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.api.rango(min, max).subscribe({
      next: (datos) => {
        this.productos.set(datos);
        this.cargando.set(false);
      },
      error: (error) => {
        this.error.set(mensajeHttp(error));
        this.cargando.set(false);
      },
    });
  }
}
