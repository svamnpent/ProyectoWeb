import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controlador: HttpTestingController;

  function configurar(token: string | null): void {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { token: () => token } },
      ],
    });
    http = TestBed.inject(HttpClient);
    controlador = TestBed.inject(HttpTestingController);
  }

  afterEach(() => controlador.verify());

  it('agrega el Bearer a las peticiones hacia la API', () => {
    configurar('abc');
    http.get(`${environment.apiUrl}/productos`).subscribe();

    const req = controlador.expectOne(`${environment.apiUrl}/productos`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush([]);
  });

  it('no envía el token a dominios externos', () => {
    configurar('abc');
    http.get('https://otro-sitio.com/datos').subscribe();

    const req = controlador.expectOne('https://otro-sitio.com/datos');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('no agrega cabecera si no hay sesión', () => {
    configurar(null);
    http.get(`${environment.apiUrl}/productos`).subscribe();

    const req = controlador.expectOne(`${environment.apiUrl}/productos`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });
});
