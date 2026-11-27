import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  EstadoIntegracion,
  RespuestaIntegracion,
  SolicitudIntegracion,
} from '../models/integracion.model';

@Injectable({ providedIn: 'root' })
export class IntegracionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/diagnostico`;

  estado(): Observable<EstadoIntegracion> {
    return this.http.get<EstadoIntegracion>(`${this.url}/estado`);
  }

  validar(datos: SolicitudIntegracion): Observable<RespuestaIntegracion> {
    return this.http.post<RespuestaIntegracion>(`${this.url}/validar`, datos);
  }
}
