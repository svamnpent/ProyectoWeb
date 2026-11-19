import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Venta } from '../../core/models/venta.model';
import { Pedido } from './pedido';

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
    {
      id: 2,
      productoId: 2,
      productoNombre: 'Teclado',
      cantidad: 1,
      precioUnitario: 50,
      subtotal: 50,
    },
  ],
};

describe('Pedido', () => {
  let fixture: ComponentFixture<Pedido>;
  let http: HttpTestingController;
  let el: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Pedido],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Pedido);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => http.verify());

  async function abrir(id: string): Promise<void> {
    fixture.componentRef.setInput('id', id);
    await fixture.whenStable();
  }

  it('consulta el pedido indicado en la ruta y muestra su resumen', async () => {
    await abrir('99');
    http.expectOne(`${environment.apiUrl}/ventas/99`).flush(venta);
    await fixture.whenStable();

    expect(el.querySelector('.pedido__orden')?.textContent).toContain('A1B2C3D4E5F6');
    expect(el.querySelectorAll('tbody tr').length).toBe(2);
    expect(el.querySelector('.pedido__total')?.textContent).toContain('S/ 350.00');
    expect(el.textContent).toContain('Mouse Gamer');
  });

  it('con un id que no es un número no llama a la API y avisa', async () => {
    await abrir('abc');

    http.expectNone(() => true);
    expect(el.querySelector('.alerta--error')?.textContent).toContain('no es válido');
  });

  it('muestra el mensaje del servidor si no tiene permiso', async () => {
    await abrir('99');
    http
      .expectOne(`${environment.apiUrl}/ventas/99`)
      .flush(
        { message: 'No tienes permiso para acceder a esta venta' },
        { status: 403, statusText: 'Forbidden' },
      );
    await fixture.whenStable();

    expect(el.querySelector('.alerta--error')?.textContent).toContain('No tienes permiso');
    expect(el.querySelector('.pedido__tarjeta')).toBeNull();
  });

  it('vuelve a consultar cuando cambia el id de la ruta', async () => {
    await abrir('99');
    http.expectOne(`${environment.apiUrl}/ventas/99`).flush(venta);
    await fixture.whenStable();

    await abrir('100');
    http
      .expectOne(`${environment.apiUrl}/ventas/100`)
      .flush({ ...venta, id: 100, numeroOrden: 'ZZZ' });
    await fixture.whenStable();

    expect(el.querySelector('.pedido__orden')?.textContent).toContain('ZZZ');
  });
});
