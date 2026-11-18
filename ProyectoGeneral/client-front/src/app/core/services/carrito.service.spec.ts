import { TestBed } from '@angular/core/testing';
import { Producto } from '../models/catalogo.model';
import { CarritoService } from './carrito.service';

const producto = (id: number, precio: number, stock: number): Producto => ({
  id,
  nombre: `Producto ${id}`,
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

  beforeEach(() => {
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

  it('vaciar deja el carrito en cero', () => {
    servicio.agregar(producto(1, 100, 5));
    servicio.vaciar();

    expect(servicio.cantidadTotal()).toBe(0);
  });
});
