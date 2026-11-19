import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Producto } from '../../core/models/catalogo.model';
import { CarritoService } from '../../core/services/carrito.service';
import { ProductoComponent } from './producto';

const mockProducto: Producto = {
  id: 10,
  nombre: 'Teclado Mecánico RGB',
  descripcion: 'Switches mecánicos táctiles, iluminación RGB configurable.',
  precio: 299.9,
  stock: 12,
  marca: 'Razer',
  imagenUrl: null,
  categoria: { id: 2, nombre: 'Periféricos', descripcion: null },
  etiquetas: [
    { id: 1, nombre: 'rgb' },
    { id: 2, nombre: 'gamer' },
  ],
};

describe('ProductoComponent', () => {
  let fixture: ComponentFixture<ProductoComponent>;
  let http: HttpTestingController;
  let carritoService: CarritoService;
  let el: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    carritoService = TestBed.inject(CarritoService);
    carritoService.vaciar();
    fixture = TestBed.createComponent(ProductoComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => http.verify());

  async function abrir(id: string): Promise<void> {
    fixture.componentRef.setInput('id', id);
    await fixture.whenStable();
  }

  it('consulta el producto por ID y muestra los datos detallados', async () => {
    await abrir('10');
    http.expectOne(`${environment.apiUrl}/productos/10`).flush(mockProducto);
    await fixture.whenStable();

    expect(el.querySelector('.detalle__titulo')?.textContent).toContain('Teclado Mecánico RGB');
    expect(el.querySelector('.detalle__marca')?.textContent).toContain('Razer');
    expect(el.querySelector('.detalle__categoria')?.textContent).toContain('Periféricos');
    expect(el.querySelector('.detalle__precio')?.textContent).toContain('S/ 299.90');
    expect(el.querySelector('.detalle__descripcion')?.textContent).toContain('Switches mecánicos');
    expect(el.querySelectorAll('.detalle__etiqueta').length).toBe(2);
  });

  it('con ID no numérico muestra mensaje de error y no contacta a la API', async () => {
    await abrir('xyz');

    http.expectNone(() => true);
    expect(el.querySelector('.alerta--error')?.textContent).toContain('no es válido');
  });

  it('muestra el mensaje de error cuando la API responde 404', async () => {
    await abrir('999');
    http
      .expectOne(`${environment.apiUrl}/productos/999`)
      .flush({ message: 'Producto no encontrado' }, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();

    expect(el.querySelector('.alerta--error')?.textContent).toContain('Producto no encontrado');
    expect(el.querySelector('.detalle__tarjeta')).toBeNull();
  });

  it('permite agregar el producto al carrito de compras', async () => {
    await abrir('10');
    http.expectOne(`${environment.apiUrl}/productos/10`).flush(mockProducto);
    await fixture.whenStable();

    const boton = el.querySelector<HTMLButtonElement>('.detalle__btn-agregar');
    expect(boton?.disabled).toBe(false);
    boton?.click();
    await fixture.whenStable();

    expect(carritoService.cantidadTotal()).toBe(1);
    expect(el.querySelector('.alerta--exito')?.textContent).toContain('se agregó al carrito');
  });

  it('con stock 0 muestra botón deshabilitado con texto Agotado', async () => {
    await abrir('10');
    http.expectOne(`${environment.apiUrl}/productos/10`).flush({ ...mockProducto, stock: 0 });
    await fixture.whenStable();

    const boton = el.querySelector<HTMLButtonElement>('.detalle__btn-agregar');
    expect(boton?.disabled).toBe(true);
    expect(boton?.textContent).toContain('Agotado');
    expect(el.querySelector('.detalle__stock--agotado')?.textContent).toContain('Sin stock');
  });

  it('vuelve a consultar cuando el ID cambia', async () => {
    await abrir('10');
    http.expectOne(`${environment.apiUrl}/productos/10`).flush(mockProducto);
    await fixture.whenStable();

    await abrir('20');
    http
      .expectOne(`${environment.apiUrl}/productos/20`)
      .flush({ ...mockProducto, id: 20, nombre: 'Mouse Óptico' });
    await fixture.whenStable();

    expect(el.querySelector('.detalle__titulo')?.textContent).toContain('Mouse Óptico');
  });
});
