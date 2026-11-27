import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Producto, ProductoEntrada } from '../models/modelos';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/productos`);
  }

  rango(min: number, max: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/productos/rango`, {
      params: { min, max },
    });
  }

  crear(producto: ProductoEntrada): Observable<Producto> {
    return this.http.post<Producto>(`${this.apiUrl}/productos`, producto);
  }

  registrarSalida(id: number, cantidad: number): Observable<Producto> {
    return this.http.post<Producto>(
      `${this.apiUrl}/inventario/${id}/salida?cantidad=${cantidad}`,
      null,
    );
  }
}
