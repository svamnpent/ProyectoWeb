import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { PerfilService } from './perfil.service';

describe('PerfilService', () => {
  let servicio: PerfilService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(PerfilService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crear envía POST /perfiles', () => {
    const solicitud = { usuarioId: 5, avatarUrl: null, biografia: 'Hola' };
    servicio.crear(solicitud).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/perfiles`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(solicitud);
    req.flush({});
  });

  it('actualizar envía PUT /perfiles/{id}', () => {
    const solicitud = { usuarioId: 5, avatarUrl: 'https://cdn.example/a.png', biografia: null };
    servicio.actualizar(8, solicitud).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/perfiles/8`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(solicitud);
    req.flush({});
  });
});
