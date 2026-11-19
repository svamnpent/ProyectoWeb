import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { Pedidos } from './pedidos';

it('consulta las compras propias y despliega sus líneas solo al solicitarlas', async () => {
  TestBed.configureTestingModule({
    imports: [Pedidos],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(Pedidos);
  const http = TestBed.inject(HttpTestingController);
  fixture.detectChanges();
  const url = environment.apiUrl + '/ventas';
  http
    .expectOne(url + '/mias')
    .flush([
      { id: 12, numeroOrden: 'ORD-12', fecha: '2026-01-25T13:00:00Z', total: 20, cantidadItems: 1 },
    ]);
  await fixture.whenStable();
  const el: HTMLElement = fixture.nativeElement;
  const boton = [...el.querySelectorAll('button')].find((b) =>
    b.textContent.includes('Ver productos'),
  )!;
  boton.click();
  http
    .expectOne(url + '/12/detalles')
    .flush([{ id: 4, productoId: 7, productoNombre: 'Teclado', cantidad: 1, precioUnitario: 20 }]);
  await fixture.whenStable();
  expect(el.querySelector('a[href="/producto/7"]')?.textContent).toContain('Teclado');
  http.verify();
});
