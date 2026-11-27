import { Injectable, computed, signal } from '@angular/core';
import { Perfil } from '../models/modelos';

/** El token vive solo en memoria: al recargar el navegador la sesión desaparece. */
@Injectable({ providedIn: 'root' })
export class SesionService {
  private readonly _token = signal<string | null>(null);
  private readonly _perfil = signal<Perfil | null>(null);

  readonly token = this._token.asReadonly();
  readonly perfil = this._perfil.asReadonly();
  readonly autenticado = computed(() => this._token() !== null && this._perfil() !== null);
  readonly esAdmin = computed(() => this._perfil()?.roles.includes('ROLE_ADMIN') ?? false);

  establecerToken(token: string): void {
    this._token.set(token);
  }

  establecerPerfil(perfil: Perfil): void {
    this._perfil.set(perfil);
  }

  cerrar(): void {
    this._token.set(null);
    this._perfil.set(null);
  }
}
