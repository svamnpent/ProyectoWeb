import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import { Perfil, Usuario } from '../models/usuario.model';

const CLAVE_TOKEN = 'phantom.token';
const CLAVE_EXPIRA = 'phantom.expira';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly _token = signal<string | null>(this.leerTokenGuardado());
  private readonly _usuario = signal<Usuario | null>(null);

  readonly token = this._token.asReadonly();
  readonly usuario = this._usuario.asReadonly();

  /** Autenticado = hay token vigente y ya se cargó el usuario (/auth/me). */
  readonly estaAutenticado = computed(() => this._token() !== null && this._usuario() !== null);
  readonly rol = computed(() => this._usuario()?.rol ?? null);
  readonly esAdmin = computed(() => this.rol() === 'ADMIN');
  readonly esStaff = computed(() => this.rol() === 'ADMIN' || this.rol() === 'TRABAJADOR');
  readonly primerNombre = computed(() => this._usuario()?.nombre.split(' ')[0] ?? '');

  login(credenciales: LoginRequest): Observable<Usuario> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, credenciales)
      .pipe(switchMap((respuesta) => this.iniciarSesion(respuesta)));
  }

  /** El backend registra y devuelve el token: el usuario queda logueado. */
  registrar(datos: RegisterRequest): Observable<Usuario> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, datos)
      .pipe(switchMap((respuesta) => this.iniciarSesion(respuesta)));
  }

  cerrarSesion(): void {
    this._token.set(null);
    this._usuario.set(null);
    this.borrarTokenGuardado();
  }

  /** Sustituye al usuario en memoria tras guardar la cuenta (la respuesta ya trae el perfil). */
  reemplazarUsuario(usuario: Usuario): void {
    this._usuario.set(usuario);
  }

  /** Conserva la cuenta y actualiza solo el perfil público (avatar y biografía). */
  aplicarPerfil(perfil: Perfil): void {
    const actual = this._usuario();
    if (actual !== null) {
      this._usuario.set({ ...actual, perfil });
    }
  }

  /** Se ejecuta al arrancar la app: si hay token guardado, recupera al usuario. */
  restaurarSesion(): Observable<unknown> {
    if (this._token() === null) {
      return of(null);
    }
    return this.cargarUsuario().pipe(
      catchError((error: { status?: number }) => {
        // Solo se descarta el token si el servidor lo rechazó; un corte de red no cierra sesión.
        if (error.status === 401) {
          this.cerrarSesion();
        }
        return of(null);
      }),
    );
  }

  private iniciarSesion(respuesta: AuthResponse): Observable<Usuario> {
    this.guardarToken(respuesta);
    return this.cargarUsuario().pipe(
      catchError((error) => {
        this.cerrarSesion();
        return throwError(() => error);
      }),
    );
  }

  private cargarUsuario(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/me`).pipe(tap((u) => this._usuario.set(u)));
  }

  // ── Persistencia del token (localStorage) ──────────────────
  private guardarToken(respuesta: AuthResponse): void {
    this._token.set(respuesta.accessToken);
    try {
      localStorage.setItem(CLAVE_TOKEN, respuesta.accessToken);
      localStorage.setItem(CLAVE_EXPIRA, String(Date.now() + respuesta.expiresIn * 1000));
    } catch {
      // Modo privado o almacenamiento bloqueado: la sesión dura solo mientras la pestaña esté abierta.
    }
  }

  private leerTokenGuardado(): string | null {
    try {
      const token = localStorage.getItem(CLAVE_TOKEN);
      const expira = Number(localStorage.getItem(CLAVE_EXPIRA));
      if (token && expira > Date.now()) {
        return token;
      }
      this.borrarTokenGuardado();
    } catch {
      // sin acceso a localStorage
    }
    return null;
  }

  private borrarTokenGuardado(): void {
    try {
      localStorage.removeItem(CLAVE_TOKEN);
      localStorage.removeItem(CLAVE_EXPIRA);
    } catch {
      // sin acceso a localStorage
    }
  }
}
