import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  TestRequest,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Producto } from '../../core/models/catalogo.model';
import { AuthService } from '../../core/services/auth.service';
import { CarritoService } from '../../core/services/carrito.service';
import { Carrito } from './carrito';

const producto = (id: number, nombre: string, precio: number, stock: number): Producto => ({
  id,
  nombre,
  descripcion: null,
  precio,
  stock,
  marca: null,
  imagenUrl: null,
  categoria: { id: 1, nombre: 'Gaming', descripcion: null },
  etiquetas: [],
});

const mouse = producto(1, 'Mouse', 100, 5);
const teclado = producto(2, 'Teclado', 50, 3);

describe('Carrito', () => {
  let fixture: ComponentFixture<Carrito>;
  let http: HttpTestingController;
  let router: Router;
  let el: HTMLElement;
  let servicio: CarritoService;

  async function crear(opciones: {
    autenticado: boolean;
    items?: [Producto, number][];
  }): Promise<void> {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Carrito],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: {
            estaAutenticado: () => opciones.autenticado,
            usuario: () => (opciones.autenticado ? { id: 7 } : null),
          },
        },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    servicio = TestBed.inject(CarritoService);
    for (const [p, cantidad] of opciones.items ?? [
      [mouse, 2],
      [teclado, 1],
    ]) {
      for (let i = 0; i < cantidad; i++) {
        servicio.agregar(p);
      }
    }
    fixture = TestBed.createComponent(Carrito);
    el = fixture.nativeElement as HTMLElement;
  }

  const pendiente = (ruta: string, metodo = 'GET'): TestRequest =>
    http
      .match((r) => r.url === `${environment.apiUrl}/${ruta}` && r.method === metodo)
      .filter((r) => !r.cancelled)[0];

  /** Responde la verificación de stock que hace el carrito al abrirse. */
  async function abrir(vigentes: Producto[] = [mouse, teclado]): Promise<void> {
    pendiente('productos').flush(vigentes);
    await fixture.whenStable();
  }

  const lineas = (): HTMLElement[] => Array.from(el.querySelectorAll('.linea'));
  const botonComprar = (): HTMLButtonElement => el.querySelector('.resumen .btn--acento')!;
  const cantidadInput = (i: number): HTMLInputElement =>
    el.querySelectorAll<HTMLInputElement>('.cantidad__input')[i];

  async function escribirCantidad(indice: number, valor: string): Promise<void> {
    const input = cantidadInput(indice);
    input.value = valor;
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  afterEach(() => http.verify());

  it('muestra una línea por producto y el total', async () => {
    await crear({ autenticado: true });
    await abrir();

    expect(lineas().length).toBe(2);
    expect(el.querySelector('.resumen__total')?.textContent).toContain('S/ 250.00');
  });

  it('con el carrito vacío no consulta la API y muestra el mensaje', async () => {
    await crear({ autenticado: true, items: [] });
    await fixture.whenStable();

    http.expectNone(`${environment.apiUrl}/productos`);
    expect(el.querySelector('.carrito__vacio')?.textContent).toContain('Tu carrito está vacío');
  });

  it('al abrirse ajusta las cantidades al stock vigente y avisa', async () => {
    await crear({ autenticado: true });
    await abrir([producto(1, 'Mouse', 100, 1), teclado]);

    expect(cantidadInput(0).value).toBe('1');
    expect(el.querySelector('.alerta--aviso')?.textContent).toContain('Se ajustó "Mouse"');
    expect(servicio.cantidadTotal()).toBe(2);
  });

  it('al cambiar una cantidad válida actualiza el carrito y el total', async () => {
    await crear({ autenticado: true });
    await abrir();

    await escribirCantidad(0, '3');

    expect(servicio.cantidadTotal()).toBe(4);
    expect(el.querySelector('.resumen__total')?.textContent).toContain('S/ 350.00');
  });

  it('no deja superar el stock: muestra el error y no cambia el carrito', async () => {
    await crear({ autenticado: true });
    await abrir();

    await escribirCantidad(0, '9');

    expect(el.querySelector('.campo__error')?.textContent).toContain('Solo hay 5 unidades');
    expect(servicio.cantidadTotal()).toBe(3);
  });

  it('los botones + y − cambian la cantidad', async () => {
    await crear({ autenticado: true });
    await abrir();
    const [menos, mas] = Array.from(
      lineas()[0].querySelectorAll<HTMLButtonElement>('.cantidad__boton'),
    );

    mas.click();
    await fixture.whenStable();
    expect(servicio.items()[0].cantidad).toBe(3);

    menos.click();
    menos.click();
    menos.click(); // no baja de 1
    await fixture.whenStable();
    expect(servicio.items()[0].cantidad).toBe(1);
  });

  it('quitar elimina la línea', async () => {
    await crear({ autenticado: true });
    await abrir();

    lineas()[0].querySelector<HTMLButtonElement>('.linea__quitar')!.click();
    await fixture.whenStable();

    expect(lineas().length).toBe(1);
    expect(servicio.items().map((i) => i.producto.id)).toEqual([2]);
  });

  it('sin sesión, comprar lleva al login conservando el carrito', async () => {
    await crear({ autenticado: false });
    await abrir();

    expect(botonComprar().textContent).toContain('Inicia sesión para comprar');
    botonComprar().click();

    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/carrito' },
    });
    http.expectNone(`${environment.apiUrl}/ventas`);
    expect(servicio.cantidadTotal()).toBe(3);
  });

  it('con sesión envía la venta, vacía el carrito y abre el pedido', async () => {
    await crear({ autenticado: true });
    await abrir();

    botonComprar().click();
    const envio = pendiente('ventas', 'POST');
    expect(envio.request.body).toEqual({
      usuarioId: 7,
      detalles: [
        { productoId: 1, cantidad: 2 },
        { productoId: 2, cantidad: 1 },
      ],
    });
    envio.flush({ id: 99, numeroOrden: 'ABC', total: 250, cantidadItems: 3, detalles: [] });
    await fixture.whenStable();

    expect(servicio.cantidadTotal()).toBe(0);
    expect(router.navigate).toHaveBeenCalledWith(['/pedido', 99]);
  });

  it('si no hay stock suficiente muestra el mensaje y actualiza las cantidades', async () => {
    await crear({ autenticado: true });
    await abrir();

    botonComprar().click();
    pendiente('ventas', 'POST').flush(
      { message: "Stock insuficiente para 'Mouse': solicitado 2, disponible 1" },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();
    await abrir([producto(1, 'Mouse', 100, 1), teclado]); // la nueva verificación de stock

    expect(el.querySelector('.alerta--error')?.textContent).toContain('Stock insuficiente');
    expect(servicio.items()[0].cantidad).toBe(1);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('con una cantidad inválida no envía la compra', async () => {
    await crear({ autenticado: true });
    await abrir();

    await escribirCantidad(0, '0');
    botonComprar().click();
    await fixture.whenStable();

    http.expectNone(`${environment.apiUrl}/ventas`);
    expect(el.querySelector('.alerta--error')?.textContent).toContain('Corrige las cantidades');
  });
});
