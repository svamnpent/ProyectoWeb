import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Rol } from '../../core/models/rol';
import { PerfilRequest, Usuario, UsuarioRequest } from '../../core/models/usuario.model';
import { Venta } from '../../core/models/venta.model';
import { AuthService } from '../../core/services/auth.service';
import { PerfilService } from '../../core/services/perfil.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { VentaService } from '../../core/services/venta.service';
import { mensajeDeError } from '../../core/utils/http-error';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class PerfilUsuario {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly perfilService = inject(PerfilService);
  private readonly ventaService = inject(VentaService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly usuario = this.auth.usuario;
  protected readonly ventas = signal<Venta[]>([]);
  protected readonly cargandoCompras = signal(true);
  protected readonly errorCompras = signal<string | null>(null);
  protected readonly pedidoAbierto = signal<number | null>(null);
  protected readonly guardandoCuenta = signal(false);
  protected readonly guardandoPerfil = signal(false);
  protected readonly mensajeCuenta = signal<string | null>(null);
  protected readonly errorCuenta = signal<string | null>(null);
  protected readonly mensajePerfil = signal<string | null>(null);
  protected readonly errorPerfil = signal<string | null>(null);
  protected readonly avatarFallido = signal(false);

  protected readonly cuentaForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(255)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    dni: ['', [Validators.pattern(/^[0-9]{8,20}$/)]],
    numeroTelefono: ['', [Validators.pattern(/^\+?[0-9]{7,15}$/)]],
    direccion: ['', [Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  protected readonly perfilForm = this.fb.nonNullable.group({
    avatarUrl: ['', [Validators.maxLength(1000)]],
    biografia: ['', [Validators.maxLength(1000)]],
  });

  constructor() {
    const actual = this.auth.usuario();
    if (!actual) {
      this.cargandoCompras.set(false);
      return;
    }
    this.rellenarCuenta(actual);
    this.rellenarPerfil(actual);
    this.cargarCompras();
  }

  protected iniciales(): string {
    const partes = (this.usuario()?.nombre ?? '').trim().split(/\s+/).filter(Boolean);
    const letras = (partes[0]?.[0] ?? '') + (partes[1]?.[0] ?? '');
    return (letras || '?').toUpperCase();
  }

  protected etiquetaRol(rol: Rol): string {
    if (rol === 'ADMIN') {
      return 'Administrador';
    }
    if (rol === 'TRABAJADOR') {
      return 'Trabajador';
    }
    return 'Cliente';
  }

  protected campoInvalido(formulario: 'cuenta' | 'perfil', nombreCampo: string): boolean {
    const control =
      formulario === 'cuenta' ? this.cuentaForm.get(nombreCampo) : this.perfilForm.get(nombreCampo);
    return Boolean(control && control.invalid && (control.touched || control.dirty));
  }

  protected alternarPedido(id: number): void {
    this.pedidoAbierto.update((actual) => (actual === id ? null : id));
  }

  protected reintentarCompras(): void {
    this.cargarCompras();
  }

  protected guardarCuenta(): void {
    const actual = this.auth.usuario();
    if (!actual || this.cuentaForm.invalid || this.guardandoCuenta()) {
      this.cuentaForm.markAllAsTouched();
      return;
    }

    const valores = this.cuentaForm.getRawValue();
    const payload: UsuarioRequest = {
      nombre: valores.nombre.trim(),
      email: valores.email.trim(),
      password: valores.password,
      dni: this.opcional(valores.dni),
      direccion: this.opcional(valores.direccion),
      numeroTelefono: this.opcional(valores.numeroTelefono),
    };

    this.guardandoCuenta.set(true);
    this.errorCuenta.set(null);
    this.mensajeCuenta.set(null);

    this.usuarioService
      .actualizar(actual.id, payload)
      .pipe(finalize(() => this.guardandoCuenta.set(false)))
      .subscribe({
        next: (usuario) => {
          this.auth.reemplazarUsuario(usuario);
          this.rellenarCuenta(usuario);
          this.cuentaForm.controls.password.reset('');
          this.mensajeCuenta.set('Datos de la cuenta actualizados.');
        },
        error: (e) =>
          this.errorCuenta.set(mensajeDeError(e, 'No se pudieron guardar los datos de la cuenta')),
      });
  }

  protected guardarPerfil(): void {
    const actual = this.auth.usuario();
    if (!actual || this.perfilForm.invalid || this.guardandoPerfil()) {
      this.perfilForm.markAllAsTouched();
      return;
    }

    const valores = this.perfilForm.getRawValue();
    const payload: PerfilRequest = {
      usuarioId: actual.id,
      avatarUrl: this.opcional(valores.avatarUrl),
      biografia: this.opcional(valores.biografia),
    };
    const perfilId = actual.perfil?.id;
    const solicitud =
      perfilId == null
        ? this.perfilService.crear(payload)
        : this.perfilService.actualizar(perfilId, payload);

    this.guardandoPerfil.set(true);
    this.errorPerfil.set(null);
    this.mensajePerfil.set(null);

    solicitud.pipe(finalize(() => this.guardandoPerfil.set(false))).subscribe({
      next: (perfil) => {
        this.auth.aplicarPerfil(perfil);
        this.rellenarPerfil({ ...actual, perfil });
        this.mensajePerfil.set('Perfil guardado.');
      },
      error: (e) => this.errorPerfil.set(mensajeDeError(e, 'No se pudo guardar el perfil')),
    });
  }

  private cargarCompras(): void {
    this.cargandoCompras.set(true);
    this.errorCompras.set(null);
    this.ventaService
      .mias()
      .pipe(
        finalize(() => this.cargandoCompras.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (ventas) => this.ventas.set(ventas),
        error: (e) =>
          this.errorCompras.set(mensajeDeError(e, 'No se pudo cargar el historial de compras')),
      });
  }

  private rellenarCuenta(usuario: Usuario): void {
    this.cuentaForm.patchValue({
      nombre: usuario.nombre,
      email: usuario.email,
      dni: usuario.dni ?? '',
      numeroTelefono: usuario.numeroTelefono ?? '',
      direccion: usuario.direccion ?? '',
    });
  }

  private rellenarPerfil(usuario: Usuario): void {
    this.perfilForm.patchValue({
      avatarUrl: usuario.perfil?.avatarUrl ?? '',
      biografia: usuario.perfil?.biografia ?? '',
    });
    this.avatarFallido.set(false);
  }

  private opcional(valor: string): string | null {
    const limpio = valor.trim();
    return limpio.length > 0 ? limpio : null;
  }
}
