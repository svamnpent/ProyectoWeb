import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { IntegracionService } from './integracion.service';

describe('IntegracionService', () => {
  let service: IntegracionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(IntegracionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('solicita el estado por GET', () => {
    service.estado().subscribe((respuesta) => {
      expect(respuesta.estado).toBe('OPERATIVO');
    });
    const req = http.expectOne(`${environment.apiUrl}/diagnostico/estado`);
    expect(req.request.method).toBe('GET');
    req.flush({ estado: 'OPERATIVO', servicio: 'Tienda UTP API', fecha: '2026-10-01' });
  });

  it('envía el contrato de validación por POST', () => {
    const cuerpo = { nombre: 'Prueba integral', monto: 125.5, fecha: '2026-10-01' };
    service.validar(cuerpo).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/diagnostico/validar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(cuerpo);
    req.flush({ mensaje: 'Datos recibidos y validados', ...cuerpo });
  });
});
