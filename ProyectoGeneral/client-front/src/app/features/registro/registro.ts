import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { RegisterRequest } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { mensajeDeError } from '../../core/utils/http-error';

function contrasenasCoinciden(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmar = control.get('confirmarPassword')?.value;
  if (!password || !confirmar) {
    return null;
  }
  return password === confirmar ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmarPassword: ['', [Validators.required]],
      dni: ['', [Validators.pattern(/^[0-9]{8,20}$/)]],
      numeroTelefono: ['', [Validators.pattern(/^\+?[0-9]{7,15}$/)]],
      direccion: ['', [Validators.maxLength(255)]],
    },
    { validators: contrasenasCoinciden },
  );

  protected campoInvalido(nombreCampo: string): boolean {
    const control = this.formulario.get(nombreCampo);
    return Boolean(control && control.invalid && (control.touched || control.dirty));
  }

  protected registrar(): void {
    if (this.formulario.invalid || this.enviando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.error.set(null);

    const valores = this.formulario.getRawValue();
    const payload: RegisterRequest = {
      nombre: valores.nombre.trim(),
      email: valores.email.trim(),
      password: valores.password,
      ...(valores.dni.trim() ? { dni: valores.dni.trim() } : {}),
      ...(valores.numeroTelefono.trim() ? { numeroTelefono: valores.numeroTelefono.trim() } : {}),
      ...(valores.direccion.trim() ? { direccion: valores.direccion.trim() } : {}),
    };

    this.auth
      .registrar(payload)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl('/'),
        error: (e) => this.error.set(mensajeDeError(e, 'No se pudo completar el registro')),
      });
  }
}