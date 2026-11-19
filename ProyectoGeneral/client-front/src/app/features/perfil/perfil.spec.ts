import { signal } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Perfil, Usuario } from '../../core/models/usuario.model';
import { Venta } from '../../core/models/venta.model';
import { AuthService } from '../../core/services/auth.service';
import { PerfilUsuario } from './perfil';

const usuarioDemo: Usuario = {
  id: 5,
  nombre: 'María Quispe',
  email: 'maria@demo.local',
  rol: 'USER',
  dni: '74859612',
  direccion: 'Av. Arequipa 123',
  numeroTelefono: '987654321',
  fechaRegistro: '2026-03-01T09:00:00-05:00',
  perfil: null,
};

const venta: Venta = {
  id: 99,
  fecha: '2026-11-18T10:30:00-05:00',
  total: 350,
  cantidadItems: 4,
  numeroOrden: 'A1B2C3D4E5F6',
  usuario: { id: 5, nombre: 'María Quispe', email: 'maria@demo.local' },
  detalles: [
    {
      id: 1,
      productoId: 1,
      productoNombre: 'Mouse Gamer',
      cantidad: 3,
      precioUnitario: 100,
      subtotal: 300,
    },
  ],
};

describe('PerfilUsuario', () => {
  let fixture: ComponentFixture<PerfilUsuario>;
  let http: HttpTestingController;
  let el: HTMLElement;
  let usuarioActual: ReturnType<typeof signal<Usuario | null>>;

  async function crear(usuario: Usuario | null = usuarioDemo): Promise<void> {
    usuarioActual = signal<Usuario | null>(usuario);
    await TestBed.configureTestingModule({
      imports: [PerfilUsuario],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: {
            usuario: usuarioActual.asReadonly(),
            reemplazarUsuario: (u: Usuario) => usuarioActual.set(u),
            aplicarPerfil: (p: Perfil) => {
              const actual = usuarioActual();
              if (actual) {
                usuarioActual.set({ ...actual, perfil: p });
              }
            },
          },
        },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(PerfilUsuario);
    el = fixture.nativeElement as HTMLElement;
  }

  async function abrirCompras(
    respuesta: Venta[] | { status: number; message: string },
  ): Promise<void> {
    const req = http.expectOne(`${environment.apiUrl}/ventas/mias`);
    if (Array.isArray(respuesta)) {
      req.flush(respuesta);
    } else {
      req.flush({ message: respuesta.message }, { status: respuesta.status, statusText: 'Error' });
    }
    await fixture.whenStable();
  }

  afterEach(() => http.verify());

  function escribir(id: string, valor: string): void {
    const input = el.querySelector<HTMLInputElement | HTMLTextAreaElement>(`#${id}`)!;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  }

  it('muestra la cuenta y el historial de compras', async () => {
    await crear();
    await abrirCompras([venta]);

    expect(el.querySelector('.perfil__nombre')?.textContent).toContain('María Quispe');
    expect(el.querySelector('.perfil__correo')?.textContent).toContain('maria@demo.local');
    expect(el.querySelector('.perfil__rol')?.textContent).toContain('Cliente');
    expect(el.textContent).toContain('A1B2C3D4E5F6');
    expect(el.textContent).toContain('S/ 350.00');

    el.querySelector<HTMLButtonElement>('.perfil__compra-cabecera')!.click();
    await fixture.whenStable();
    expect(el.textContent).toContain('Mouse Gamer');
  });

  it('sin compras invita a ir al catálogo', async () => {
    await crear();
    await abrirCompras([]);

    expect(el.querySelector('.perfil__vacio')?.textContent).toContain('Todavía no tienes compras');
    expect(el.querySelector('.perfil__vacio a')?.getAttribute('href')).toBe('/');
  });

  it('si el historial falla muestra el mensaje y permite reintentar', async () => {
    await crear();
    await abrirCompras({ status: 500, message: 'No se pudo leer las ventas' });

    expect(el.querySelector('.alerta--error')?.textContent).toContain('No se pudo leer las ventas');

    el.querySelector<HTMLButtonElement>('.perfil__historial button')!.click();
    await abrirCompras([venta]);
    expect(el.textContent).toContain('A1B2C3D4E5F6');
  });

  it('guarda la cuenta y refleja el nombre nuevo', async () => {
    await crear();
    await abrirCompras([]);

    escribir('cuenta-nombre', 'María López');
    escribir('cuenta-password', 'Demo12345');
    el.querySelectorAll('form')[0].dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    const req = http.expectOne(`${environment.apiUrl}/usuarios/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({
      nombre: 'María López',
      email: 'maria@demo.local',
      password: 'Demo12345',
      dni: '74859612',
      direccion: 'Av. Arequipa 123',
      numeroTelefono: '987654321',
    });
    req.flush({ ...usuarioDemo, nombre: 'María López' });
    await fixture.whenStable();

    expect(el.querySelector('.perfil__nombre')?.textContent).toContain('María López');
    expect(el.querySelector('.alerta--exito')?.textContent).toContain(
      'Datos de la cuenta actualizados',
    );
    expect(el.querySelector<HTMLInputElement>('#cuenta-password')?.value).toBe('');
  });

  it('no guarda la cuenta si falta la contraseña', async () => {
    await crear();
    await abrirCompras([]);

    el.querySelectorAll('form')[0].dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    expect(el.querySelector('#cuenta-password')?.closest('.campo')?.textContent).toContain(
      'entre 8 y 72 caracteres',
    );
    http.expectNone(`${environment.apiUrl}/usuarios/5`);
  });

  it('crea el perfil cuando la cuenta todavía no tiene uno', async () => {
    await crear();
    await abrirCompras([]);

    escribir('perfil-avatar', 'https://cdn.example/maria.png');
    escribir('perfil-bio', 'Me gustan los periféricos.');
    el.querySelectorAll('form')[1].dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    const req = http.expectOne(`${environment.apiUrl}/perfiles`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      usuarioId: 5,
      avatarUrl: 'https://cdn.example/maria.png',
      biografia: 'Me gustan los periféricos.',
    });
    req.flush({
      id: 8,
      avatarUrl: 'https://cdn.example/maria.png',
      biografia: 'Me gustan los periféricos.',
      usuarioId: 5,
    });
    await fixture.whenStable();

    expect(el.querySelector('.perfil__bio')?.textContent).toContain('Me gustan los periféricos.');
    expect(usuarioActual()?.perfil?.id).toBe(8);
  });

  it('actualiza el perfil existente', async () => {
    await crear({
      ...usuarioDemo,
      perfil: { id: 8, avatarUrl: null, biografia: 'Antes', usuarioId: 5 },
    });
    await abrirCompras([]);

    escribir('perfil-bio', 'Después');
    el.querySelectorAll('form')[1].dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    const req = http.expectOne(`${environment.apiUrl}/perfiles/8`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.biografia).toBe('Después');
    req.flush({ id: 8, avatarUrl: null, biografia: 'Después', usuarioId: 5 });
    await fixture.whenStable();

    expect(el.querySelector('.perfil__bio')?.textContent).toContain('Después');
  });

  it('sin sesión no consulta compras y pide ingresar', async () => {
    await crear(null);
    await fixture.whenStable();

    http.expectNone(() => true);
    expect(el.textContent).toContain('Inicia sesión');
  });
});
