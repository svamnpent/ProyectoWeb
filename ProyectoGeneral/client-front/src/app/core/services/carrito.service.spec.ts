import { TestBed } from '@angular/core/testing';
import { Producto } from '../models/catalogo.model';
import { CarritoService } from './carrito.service';

const producto = (
  id: number,
  precio: number,
  stock: number,
  nombre = `Producto ${id}`,
): Producto => ({
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

describe('CarritoService', () => {
  let servicio: CarritoService;

  /** Simula recargar la página: servicio nuevo que lee de localStorage. */
  const recargar = (): CarritoService => {
    TestBed.resetTestingModule();
    return TestBed.inject(CarritoService);
  };

  beforeEach(() => {
    localStorage.clear();
    servicio = TestBed.inject(CarritoService);
  });

  it('agrega productos y calcula cantidad y total', () => {
    servicio.agregar(producto(1, 100, 5));
    servicio.agregar(producto(1, 100, 5));
    servicio.agregar(producto(2, 50, 5));

    expect(servicio.cantidadTotal()).toBe(3);
    expect(servicio.total()).toBe(250);
    expect(servicio.items().length).toBe(2);
  });

  it('no supera el stock disponible', () => {
    const p = producto(1, 100, 2);

    expect(servicio.agregar(p)).toBe(true);
    expect(servicio.agregar(p)).toBe(true);
    expect(servicio.agregar(p)).toBe(false);
    expect(servicio.cantidadTotal()).toBe(2);
  });

  it('no agrega productos sin stock', () => {
    expect(servicio.agregar(producto(1, 100, 0))).toBe(false);
    expect(servicio.cantidadTotal()).toBe(0);
  });

  describe('establecerCantidad', () => {
    beforeEach(() => servicio.agregar(producto(1, 100, 5)));

    it('acepta una cantidad entera entre 1 y el stock', () => {
      expect(servicio.establecerCantidad(1, 4)).toBe(true);
      expect(servicio.cantidadTotal()).toBe(4);
    });

    it('rechaza cero, decimales, negativos y más que el stock', () => {
      for (const invalida of [0, -1, 1.5, 6]) {
        expect(servicio.establecerCantidad(1, invalida)).toBe(false);
      }
      expect(servicio.cantidadTotal()).toBe(1);
    });

    it('rechaza productos que no están en el carrito', () => {
      expect(servicio.establecerCantidad(99, 1)).toBe(false);
    });
  });

  it('quitar elimina el producto y vaciar deja el carrito en cero', () => {
    servicio.agregar(producto(1, 100, 5));
    servicio.agregar(producto(2, 50, 5));

    servicio.quitar(1);
    expect(servicio.items().map((i) => i.producto.id)).toEqual([2]);

    servicio.vaciar();
    expect(servicio.cantidadTotal()).toBe(0);
  });

  describe('persistencia', () => {
    it('sobrevive a una recarga', () => {
      servicio.agregar(producto(1, 100, 5));
      servicio.establecerCantidad(1, 3);

      const tras = recargar();

      expect(tras.cantidadTotal()).toBe(3);
      expect(tras.items()[0].producto.nombre).toBe('Producto 1');
    });

    it('vaciar también limpia lo guardado', () => {
      servicio.agregar(producto(1, 100, 5));
      servicio.vaciar();

      expect(recargar().cantidadTotal()).toBe(0);
    });

    it('ignora contenido corrupto o con forma inesperada', () => {
      for (const basura of ['no-es-json', '{"x":1}', '[{"producto":{"id":"a"},"cantidad":1}]']) {
        localStorage.setItem('phantom.carrito', basura);
        expect(recargar().cantidadTotal()).toBe(0);
      }
    });
  });

  describe('sincronizar', () => {
    it('quita productos eliminados o agotados y lo avisa', () => {
      servicio.agregar(producto(1, 100, 5, 'Mouse'));
      servicio.agregar(producto(2, 50, 5, 'Teclado'));
      servicio.agregar(producto(3, 20, 5, 'Alfombrilla'));

      const avisos = servicio.sincronizar([producto(2, 50, 0, 'Teclado'), producto(3, 20, 5)]);

      expect(servicio.items().map((i) => i.producto.id)).toEqual([3]);
      expect(avisos.some((a) => a.includes('Mouse') && a.includes('ya no está disponible'))).toBe(
        true,
      );
      expect(avisos.some((a) => a.includes('Teclado') && a.includes('se agotó'))).toBe(true);
    });

    it('ajusta la cantidad al stock vigente', () => {
      servicio.agregar(producto(1, 100, 5, 'Mouse'));
      servicio.establecerCantidad(1, 4);

      const avisos = servicio.sincronizar([producto(1, 100, 2, 'Mouse')]);

      expect(servicio.items()[0].cantidad).toBe(2);
      expect(avisos[0]).toContain('2 unidad(es)');
    });

    it('refresca el precio y avisa del cambio', () => {
      servicio.agregar(producto(1, 100, 5, 'Mouse'));

      const avisos = servicio.sincronizar([producto(1, 120, 5, 'Mouse')]);

      expect(servicio.total()).toBe(120);
      expect(avisos[0]).toContain('S/ 120.00');
    });

    it('no genera avisos si todo coincide', () => {
      servicio.agregar(producto(1, 100, 5));

      expect(servicio.sincronizar([producto(1, 100, 5)])).toEqual([]);
    });
  });
});
