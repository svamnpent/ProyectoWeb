import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { IntegracionService } from '../../core/integracion.service';
import { EstadoIntegracion, RespuestaIntegracion } from '../../models/integracion.model';

function correoOpcional(control: AbstractControl): ValidationErrors | null {
  const valor = String(control.value ?? '').trim();
  return valor.length === 0 ? null : Validators.email(control);
}

@Component({
  selector: 'app-diagnostico',
  imports: [ReactiveFormsModule],
  templateUrl: './diagnostico.html',
  styleUrl: './diagnostico.css',
})
export class Diagnostico {
  private readonly api = inject(IntegracionService);

  readonly estado = signal<EstadoIntegracion | null>(null);
  readonly respuesta = signal<RespuestaIntegracion | null>(null);
  readonly error = signal('');
  readonly cargando = signal(false);
  readonly formulario = new FormGroup({
    nombre: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3)],
    }),
    monto: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
    fecha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    correo: new FormControl('', {
      nonNullable: true,
      validators: [correoOpcional],
    }),
  });

  consultar(): void {
    this.error.set('');
    this.cargando.set(true);
    this.api.estado().subscribe({
      next: (dato) => {
        this.estado.set(dato);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(this.mensaje(e));
        this.cargando.set(false);
      },
    });
  }

  enviar(): void {
    this.respuesta.set(null);
    this.error.set('');
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const datos = this.formulario.getRawValue();
    if (datos.monto === null) {
      return;
    }
    this.cargando.set(true);
    this.api
      .validar({
        nombre: datos.nombre,
        monto: datos.monto,
        fecha: datos.fecha,
        ...(datos.correo.trim() ? { correo: datos.correo.trim() } : {}),
      })
      .subscribe({
        next: (r) => {
          this.respuesta.set(r);
          this.cargando.set(false);
        },
        error: (e) => {
          this.error.set(this.mensaje(e));
          this.cargando.set(false);
        },
      });
  }

  private mensaje(e: HttpErrorResponse): string {
    if (e.status === 0) {
      return 'Sin respuesta de API: revise red, CORS o servidor.';
    }
    if (e.status === 401) {
      return 'Sesión ausente o JWT vencido.';
    }
    if (e.status === 403) {
      return 'No tiene permisos para esta operación.';
    }
    if (e.status === 404) {
      return 'Ruta no encontrada: revise la URL.';
    }
    if (typeof e.error?.mensaje === 'string') {
      return e.error.mensaje;
    }
    return `Error HTTP ${e.status}. Revise Network.`;
  }
}
