import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  TestRequest,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Categoria, Producto } from '../../core/models/catalogo.model';
import { CarritoService } from '../../core/services/carrito.service';
import { Inicio } from './inicio';

const gaming: Categoria = { id: 1, nombre: 'Gaming', descripcion: null };
const audio: Categoria = { id: 2, nombre: 'Audio', descripcion: null };

const producto = (id: number, nombre: string, categoria: Categoria, stock = 10): Producto => ({
  id,
  nombre,
  descripcion: null,
  precio: 100,
  stock,
  marca: null,
  imagenUrl: null,
  categoria,
  etiquetas: [],
});

describe('Inicio (catálogo)', () => {
  let fixture: ComponentFixture<Inicio>;
  let http: HttpTestingController;
  let el: HTMLElement;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Inicio],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Inicio);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => http.verify());

  /** Primera petición aún abierta a /ruta (ignora las canceladas por forkJoin). */
  function pendiente(ruta: string): TestRequest {
    return http.match(`${environment.apiUrl}/${ruta}`).filter((r) => !r.cancelled)[0];
  }

  async function responder(productos: Producto[]): Promise<void> {
    pendiente('productos').flush(productos);
    pendiente('categorias').flush([gaming, audio]);
    await fixture.whenStable();
  }

  const nombres = (): string[] =>
    Array.from(el.querySelectorAll('.tarjeta__nombre')).map((n) => n.textContent?.trim() ?? '');

  it('muestra "Cargando" y luego las tarjetas con lo que devuelve la API', async () => {
    await fixture.whenStable();
    expect(el.textContent).toContain('Cargando productos');

    await responder([producto(1, 'Mouse', gaming), producto(2, 'Sony WH', audio)]);

    expect(nombres()).toEqual(['Mouse', 'Sony WH']);
    expect(el.textContent).not.toContain('Cargando productos');
  });

  it('filtra por categoría', async () => {
    await responder([producto(1, 'Mouse', gaming), producto(2, 'Sony WH', audio)]);

    const filtros = Array.from(el.querySelectorAll<HTMLButtonElement>('.filtro'));
    filtros.find((b) => b.textContent?.includes('Audio'))?.click();
    await fixture.whenStable();

    expect(nombres()).toEqual(['Sony WH']);
  });

  it('filtra por texto de búsqueda', async () => {
    await responder([producto(1, 'Mouse', gaming), producto(2, 'Sony WH', audio)]);

    const buscador = el.querySelector<HTMLInputElement>('.catalogo__buscador')!;
    buscador.value = 'mou';
    buscador.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    expect(nombres()).toEqual(['Mouse']);
  });

  it('muestra un error y permite reintentar si la API falla', async () => {
    http
      .expectOne(`${environment.apiUrl}/productos`)
      .flush({ message: 'Servidor caído' }, { status: 500, statusText: 'Error' });
    // forkJoin cancela /categorias cuando /productos falla, por eso no se responde aquí.
    await fixture.whenStable();

    expect(el.querySelector('.alerta--error')?.textContent).toContain('Servidor caído');

    el.querySelector<HTMLButtonElement>('.catalogo__reintentar')?.click();
    await responder([producto(1, 'Mouse', gaming)]);

    expect(nombres()).toEqual(['Mouse']);
  });

  it('agregar al carrito actualiza el CarritoService', async () => {
    await responder([producto(1, 'Mouse', gaming)]);

    el.querySelector<HTMLButtonElement>('.tarjeta__boton')?.click();
    await fixture.whenStable();

    expect(TestBed.inject(CarritoService).cantidadTotal()).toBe(1);
    expect(el.querySelector('.alerta--exito')?.textContent).toContain('Mouse');
  });
});
