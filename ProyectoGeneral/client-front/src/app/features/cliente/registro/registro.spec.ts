import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { Registro } from './registro';

it('registra al comprador, carga su sesión y vuelve al carrito', async () => {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [Registro],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const f = TestBed.createComponent(Registro);
  const http = TestBed.inject(HttpTestingController);
  const navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
  f.componentRef.setInput('returnUrl', '/carrito');
  await f.whenStable();
  const el: HTMLElement = f.nativeElement;
  const valores = {
    nombre: 'Ana',
    email: 'ana@test.local',
    clave: 'Secreta123',
    confirmar: 'Secreta123',
  };
  for (const [campo, valor] of Object.entries(valores)) {
    const input = el.querySelector<HTMLInputElement>('input[name="' + campo + '"]')!;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  }
  el.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  const req = http.expectOne(environment.apiUrl + '/auth/register');
  expect(req.request.body).toEqual({
    nombre: 'Ana',
    email: 'ana@test.local',
    password: 'Secreta123',
  });
  req.flush({ accessToken: 'test', expiresIn: 60, tokenType: 'Bearer' });
  http.expectOne(environment.apiUrl + '/auth/me').flush({ id: 5, nombre: 'Ana', rol: 'USER' });
  await f.whenStable();
  expect(navegar).toHaveBeenCalledWith('/carrito');
  http.verify();
  localStorage.clear();
});
