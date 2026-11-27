import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, Perfil, TokenResponse } from '../models/modelos';
import { SesionService } from './sesion.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sesion = inject(SesionService);

  login(datos: LoginRequest): Observable<Perfil> {
    return this.http.post<TokenResponse>(`${environment.apiUrl}/auth/login`, datos).pipe(
      switchMap((respuesta) => {
        this.sesion.establecerToken(respuesta.accessToken);
        return this.http.get<Perfil>(`${environment.apiUrl}/auth/me`).pipe(
          tap((perfil) => this.sesion.establecerPerfil(perfil)),
          catchError((error) => {
            this.sesion.cerrar();
            return throwError(() => error);
          }),
        );
      }),
    );
  }

  cerrarSesion(): void {
    this.sesion.cerrar();
  }
}
