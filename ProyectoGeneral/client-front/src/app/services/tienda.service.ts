import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto, Categoria, Venta, VentaRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class TiendaService {
  private readonly baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/productos`);
  }

  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.baseUrl}/categorias`);
  }

  buscarProductos(keyword: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/productos/buscar?keyword=${encodeURIComponent(keyword)}`);
  }

  crearVenta(venta: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(`${this.baseUrl}/ventas`, venta);
  }

  getVentas(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.baseUrl}/ventas`);
  }
}

