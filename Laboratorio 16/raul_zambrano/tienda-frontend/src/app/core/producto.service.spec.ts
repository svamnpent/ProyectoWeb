import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { Producto } from '../models/modelos';
import { ProductoService } from './producto.service';

describe('ProductoService / contrato HTTP', () => {
  let service: ProductoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('consume GET de productos y entrega datos tipados', () => {
    const productos: Producto[] = [
      { id: 1, nombre: 'Mouse', categoria: 'Tecnologia', precio: 90, stock: 5 },
    ];
    service.listar().subscribe((data) => {
      expect(data).toEqual(productos);
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/productos`);
    expect(req.request.method).toBe('GET');
    req.flush(productos);
  });

  it('envía POST de salida con el parámetro cantidad', () => {
    service.registrarSalida(1, 2).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/inventario/1/salida?cantidad=2`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, nombre: 'Mouse', categoria: 'Tecnologia', precio: 90, stock: 3 });
  });
});
