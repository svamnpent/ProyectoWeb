import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { UsuarioService } from './usuario.service';

describe('UsuarioService', () => {
  let servicio: UsuarioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(UsuarioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('actualizar envía PUT /usuarios/{id}', () => {
    const solicitud = {
      nombre: 'María Quispe',
      email: 'maria@demo.local',
      password: 'Demo12345',
      dni: null,
      direccion: 'Lima',
      numeroTelefono: null,
    };
    servicio.actualizar(5, solicitud).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/usuarios/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(solicitud);
    req.flush({});
  });
});
