import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Perfil, PerfilRequest } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/perfiles`;

  crear(solicitud: PerfilRequest): Observable<Perfil> {
    return this.http.post<Perfil>(this.apiUrl, solicitud);
  }

  actualizar(id: number, solicitud: PerfilRequest): Observable<Perfil> {
    return this.http.put<Perfil>(`${this.apiUrl}/${id}`, solicitud);
  }
}
