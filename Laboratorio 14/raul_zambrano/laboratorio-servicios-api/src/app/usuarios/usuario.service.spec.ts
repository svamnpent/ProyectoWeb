import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
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

  it('obtiene la lista con GET', () => {
    servicio.obtenerTodos().subscribe();
    const req = http.expectOne(environment.apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('crea un usuario con POST', () => {
    const entrada = {
      name: 'Ana Pérez',
      username: 'aperez',
      email: 'ana@correo.com',
      phone: '999888777',
      website: 'ana.dev',
    };
    servicio.crear(entrada).subscribe();
    const req = http.expectOne(environment.apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(entrada);
    req.flush({ id: 11, ...entrada });
  });

  it('reemplaza con PUT y modifica con PATCH', () => {
    servicio.actualizar(3, {
      name: 'Ana',
      username: 'ana',
      email: 'ana@correo.com',
      phone: '1111111',
      website: '',
    }).subscribe();
    servicio.actualizarParcial(3, { email: 'nuevo@correo.com' }).subscribe();
    const solicitudes = http.match(`${environment.apiUrl}/3`);
    expect(solicitudes.map((item) => item.request.method)).toEqual(['PUT', 'PATCH']);
    solicitudes.forEach((item) => item.flush({}));
  });

  it('elimina con DELETE', () => {
    servicio.eliminar(4).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/4`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
