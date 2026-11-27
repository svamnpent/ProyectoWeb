import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { mensajeHttp } from '../../core/http-error';
import { ProductoService } from '../../core/producto.service';

@Component({
  selector: 'app-producto-nuevo',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './producto-nuevo.html',
  styleUrl: './producto-nuevo.css',
})
export class ProductoNuevo {
  private readonly api = inject(ProductoService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly enviando = signal(false);
  protected readonly error = signal('');
  protected readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    categoria: ['', Validators.required],
    precio: [0, [Validators.required, Validators.min(0.01)]],
    stock: [0, [Validators.required, Validators.min(0)]],
  });

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.error.set('Complete nombre, categoría, precio y stock.');
      return;
    }
    this.enviando.set(true);
    this.error.set('');
    this.api.crear(this.formulario.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/productos'),
      error: (error) => {
        this.error.set(mensajeHttp(error));
        this.enviando.set(false);
      },
    });
  }
}
