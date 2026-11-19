import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Usuario } from '../models/usuario.model';
import { AuthService } from './auth.service';

const usuarioDemo: Usuario = {
  id: 1,
  nombre: 'Stefano Valdeos',
  email: 'demo@phantom.com',
  rol: 'ADMIN',
  dni: null,
  direccion: null,
  numeroTelefono: null,
  fechaRegistro: '2026-01-01T00:00:00-05:00',
  perfil: null,
};

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function hacerLogin(): void {
    service.login({ email: 'demo@phantom.com', password: 'Demo12345' }).subscribe();
    http
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ accessToken: 'token-123', tokenType: 'Bearer', expiresIn: 3600 });
    http.expectOne(`${environment.apiUrl}/auth/me`).flush(usuarioDemo);
  }

  it('login guarda el token, carga el usuario y expone el rol', () => {
    hacerLogin();

    expect(service.estaAutenticado()).toBe(true);
    expect(service.token()).toBe('token-123');
    expect(service.primerNombre()).toBe('Stefano');
    expect(service.esAdmin()).toBe(true);
    expect(localStorage.getItem('phantom.token')).toBe('token-123');
  });

  it('cerrarSesion limpia el estado y el almacenamiento', () => {
    hacerLogin();
    service.cerrarSesion();

    expect(service.estaAutenticado()).toBe(false);
    expect(service.token()).toBeNull();
    expect(localStorage.getItem('phantom.token')).toBeNull();
  });

  it('si /auth/me falla tras el login, no deja la sesión a medias', () => {
    let fallo = false;
    service.login({ email: 'a@a.com', password: 'x' }).subscribe({ error: () => (fallo = true) });
    http
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ accessToken: 't', tokenType: 'Bearer', expiresIn: 3600 });
    http
      .expectOne(`${environment.apiUrl}/auth/me`)
      .flush({ message: 'x' }, { status: 500, statusText: 'Error' });

    expect(fallo).toBe(true);
    expect(service.token()).toBeNull();
  });

  it('reemplazarUsuario y aplicarPerfil actualizan la sesión en memoria', () => {
    hacerLogin();

    service.reemplazarUsuario({ ...usuarioDemo, nombre: 'Ana López' });
    expect(service.primerNombre()).toBe('Ana');

    service.aplicarPerfil({
      id: 4,
      avatarUrl: 'https://cdn.example/ana.png',
      biografia: 'Hola',
      usuarioId: 1,
    });
    expect(service.usuario()?.perfil?.biografia).toBe('Hola');
    expect(service.usuario()?.nombre).toBe('Ana López');
  });
});
