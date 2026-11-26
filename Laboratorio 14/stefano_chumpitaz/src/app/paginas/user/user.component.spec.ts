import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserComponent } from './user.component';
import { User } from '../../models/user';

const API = 'https://jsonplaceholder.typicode.com/users';
const usuarios: User[] = [
  { id: 1, name: 'Ana Pérez', username: 'ana', email: 'ana@mail.com', phone: '111' },
  { id: 2, name: 'Luis Gómez', username: 'luis', email: 'luis@mail.com', phone: '222' },
];

describe('UserComponent', () => {
  let fixture: ComponentFixture<UserComponent>;
  let http: HttpTestingController;
  let el: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(UserComponent);
    el = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
    http.expectOne(API).flush(usuarios);
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('carga y muestra los usuarios al iniciar', () => {
    expect(el.querySelectorAll('tbody tr').length).toBe(2);
    expect(el.textContent).toContain('Ana Pérez');
  });

  it('elimina un usuario (DELETE) y lo quita de la tabla', async () => {
    el.querySelector<HTMLButtonElement>('button.peligro')!.click();
    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
    await fixture.whenStable();
    expect(el.querySelectorAll('tbody tr').length).toBe(1);
    expect(el.querySelector('tbody')!.textContent).not.toContain('Ana Pérez');
  });

  it('crea un usuario (POST) y lo agrega a la tabla', async () => {
    const set = (i: number, v: string) => {
      const input = el.querySelectorAll<HTMLInputElement>('form input')[i];
      input.value = v;
      input.dispatchEvent(new Event('input'));
    };
    set(0, 'Marta Ríos');
    set(1, 'marta');
    set(2, 'marta@mail.com');
    el.querySelector<HTMLButtonElement>('form button[type=submit]')!.click();
    const req = http.expectOne(API);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 11, name: 'Marta Ríos', username: 'marta', email: 'marta@mail.com', phone: '' });
    await fixture.whenStable();
    expect(el.querySelectorAll('tbody tr').length).toBe(3);
  });

  it('no envía el formulario si es inválido', async () => {
    el.querySelector<HTMLButtonElement>('form button[type=submit]')!.click();
    await fixture.whenStable();
    http.expectNone(API);
    expect(el.querySelectorAll('.error').length).toBeGreaterThan(0);
  });

  it('muestra un mensaje si la carga falla', async () => {
    el.querySelector<HTMLButtonElement>('.panel__head button')!.click();
    http.expectOne(API).flush('error', { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();
    expect(el.querySelector('.aviso--error')?.textContent).toContain('No se pudo cargar');
  });
});
