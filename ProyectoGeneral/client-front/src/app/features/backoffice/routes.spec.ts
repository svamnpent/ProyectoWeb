import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../app.routes';
import { Rol } from '../../core/models/rol';
import { AuthService } from '../../core/services/auth.service';

@Component({ template: '' })
class DestinoPublico {}

describe('Acceso por URL al back-office', () => {
  let rol: Rol | null;
  beforeEach(() => {
    rol = null;
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          ...routes.filter((r) => r.path === 'backoffice'),
          { path: 'login', component: DestinoPublico },
          { path: '', component: DestinoPublico },
        ]),
        {
          provide: AuthService,
          useValue: {
            rol: () => rol,
            estaAutenticado: () => rol !== null,
            esAdmin: () => rol === 'ADMIN',
          },
        },
      ],
    });
  });

  it('conserva el destino al pedir login a un visitante', async () => {
    await RouterTestingHarness.create('/backoffice/ventas');
    const router = TestBed.inject(Router);
    expect(router.url.split('?')[0]).toBe('/login');
    expect(router.parseUrl(router.url).queryParams['returnUrl']).toBe('/backoffice/ventas');
  });

  it('envía al comprador al inicio', async () => {
    rol = 'USER';
    await RouterTestingHarness.create('/backoffice/productos');
    expect(TestBed.inject(Router).url).toBe('/');
  });

  it.each(['usuarios', 'perfiles'])(
    'protege %s incluso navegando desde otra sección',
    async (ruta) => {
      rol = 'TRABAJADOR';
      const harness = await RouterTestingHarness.create('/backoffice/ventas');
      expect(harness.routeNativeElement?.textContent).toContain('Venta de mostrador');
      expect(harness.routeNativeElement?.querySelector('a[href$="/usuarios"]')).toBeNull();
      await harness.navigateByUrl('/backoffice/' + ruta);
      expect(TestBed.inject(Router).url).toBe('/');
    },
  );

  it.each(['usuarios', 'perfiles'])('permite %s al administrador', async (ruta) => {
    rol = 'ADMIN';
    await RouterTestingHarness.create('/backoffice/' + ruta);
    expect(TestBed.inject(Router).url).toBe('/backoffice/' + ruta);
  });
});
