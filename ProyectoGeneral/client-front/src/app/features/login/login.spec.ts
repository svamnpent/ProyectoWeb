import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;
  let router: Router;
  let el: HTMLElement;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture = TestBed.createComponent(Login);
    el = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  async function escribir(email: string, password: string): Promise<void> {
    const emailInput = el.querySelector<HTMLInputElement>('#email')!;
    const passwordInput = el.querySelector<HTMLInputElement>('#password')!;
    emailInput.value = email;
    emailInput.dispatchEvent(new Event('input'));
    passwordInput.value = password;
    passwordInput.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  const enviar = async (): Promise<void> => {
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  };

  it('con campos vacíos muestra los errores y no llama a la API', async () => {
    await enviar();

    expect(el.querySelectorAll('.campo__error').length).toBe(2);
    http.expectNone(`${environment.apiUrl}/auth/login`);
  });

  it('con un correo mal formado pide uno válido', async () => {
    await escribir('no-es-correo', 'Demo12345');
    await enviar();

    expect(el.querySelector('.campo__error')?.textContent).toContain('correo válido');
    http.expectNone(`${environment.apiUrl}/auth/login`);
  });

  it('con credenciales correctas inicia sesión y navega al inicio', async () => {
    await escribir('maria.quispe@demo.local', 'Demo12345');
    await enviar();

    const login = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(login.request.body).toEqual({ email: 'maria.quispe@demo.local', password: 'Demo12345' });
    login.flush({ accessToken: 'tok', tokenType: 'Bearer', expiresIn: 3600 });
    http.expectOne(`${environment.apiUrl}/auth/me`).flush({
      id: 1,
      nombre: 'María Quispe',
      email: 'maria.quispe@demo.local',
      rol: 'USER',
      dni: null,
      direccion: null,
      numeroTelefono: null,
      fechaRegistro: '2026-01-01T00:00:00-05:00',
      perfil: null,
    });
    await fixture.whenStable();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('si la API rechaza las credenciales muestra el mensaje del servidor', async () => {
    await escribir('maria.quispe@demo.local', 'incorrecta');
    await enviar();

    http
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ message: 'Credenciales inválidas' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(el.querySelector('.alerta--error')?.textContent).toContain('Credenciales inválidas');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('respeta returnUrl solo si es una ruta interna', async () => {
    fixture.componentRef.setInput('returnUrl', 'https://sitio-malo.com');
    await escribir('a@a.com', 'x');
    await enviar();

    http
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ accessToken: 't', tokenType: 'Bearer', expiresIn: 3600 });
    http.expectOne(`${environment.apiUrl}/auth/me`).flush({
      id: 1,
      nombre: 'A',
      email: 'a@a.com',
      rol: 'USER',
      dni: null,
      direccion: null,
      numeroTelefono: null,
      fechaRegistro: '2026-01-01T00:00:00-05:00',
      perfil: null,
    });
    await fixture.whenStable();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });
});
