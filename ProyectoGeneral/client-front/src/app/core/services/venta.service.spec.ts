import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { VentaService } from './venta.service';

describe('VentaService', () => {
  let servicio: VentaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(VentaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crear envía POST /ventas con la solicitud', () => {
    const solicitud = { usuarioId: 3, detalles: [{ productoId: 1, cantidad: 2 }] };
    servicio.crear(solicitud).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/ventas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(solicitud);
    req.flush({});
  });

  it('obtener consulta GET /ventas/{id}', () => {
    servicio.obtener(42).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/ventas/42`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
