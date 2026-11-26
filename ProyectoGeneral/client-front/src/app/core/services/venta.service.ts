import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Venta, VentaRequest } from '../models/venta.model';

@Injectable({ providedIn: 'root' })
export class VentaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/ventas`;

  /** Registra la compra. El servidor descuenta el stock y calcula precios y total. */
  crear(solicitud: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(this.apiUrl, solicitud);
  }

  /** Solo el dueño de la venta o el personal de la tienda pueden consultarla. */
  obtener(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${this.apiUrl}/${id}`);
  }

  /** Historial de compras del usuario autenticado, de la más reciente a la más antigua. */
  mias(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.apiUrl}/mias`);
  }
}
