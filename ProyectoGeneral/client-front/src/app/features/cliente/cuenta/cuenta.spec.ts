import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { Cuenta } from './cuenta';

it('usa el ID del perfil y el ID del titular al editar una biografía', async () => {
  TestBed.configureTestingModule({
    imports: [Cuenta],
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: AuthService, useValue: { usuario: () => ({ id: 5 }) } },
    ],
  });
  const f = TestBed.createComponent(Cuenta);
  const http = TestBed.inject(HttpTestingController);
  const base = environment.apiUrl;
  const perfil = { id: 22, usuarioId: 5, biografia: 'Inicial', avatarUrl: '' };
  f.detectChanges();
  http
    .expectOne(base + '/usuarios/5')
    .flush({ id: 5, nombre: 'Ana', email: 'ana@test.local', perfil });
  await f.whenStable();
  const el: HTMLElement = f.nativeElement;
  [...el.querySelectorAll('button')].find((b) => b.textContent.trim() === 'Perfil')!.click();
  http.expectOne(base + '/perfiles/22').flush(perfil);
  await f.whenStable();
  const texto = el.querySelector('textarea')!;
  texto.value = 'Actualizada';
  texto.dispatchEvent(new Event('input'));
  el.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  const req = http.expectOne(base + '/perfiles/22');
  expect(req.request.method).toBe('PUT');
  expect(req.request.body).toEqual({ usuarioId: 5, avatarUrl: '', biografia: 'Actualizada' });
  req.flush({ ...perfil, biografia: 'Actualizada' });
  await f.whenStable();
  http.verify();
});
