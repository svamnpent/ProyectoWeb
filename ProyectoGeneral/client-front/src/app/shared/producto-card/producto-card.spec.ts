import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Producto } from '../../core/models/catalogo.model';
import { ProductoCard } from './producto-card';

const base: Producto = {
  id: 7,
  nombre: 'Mouse Logitech G203',
  descripcion: null,
  precio: 119,
  stock: 35,
  marca: 'Logitech',
  imagenUrl: null,
  categoria: { id: 1, nombre: 'Gaming', descripcion: null },
  etiquetas: [],
};

describe('ProductoCard', () => {
  let fixture: ComponentFixture<ProductoCard>;

  async function crear(producto: Producto): Promise<HTMLElement> {
    await TestBed.configureTestingModule({
      imports: [ProductoCard],
      providers: [provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(ProductoCard);
    fixture.componentRef.setInput('producto', producto);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('muestra nombre, marca, categoría y precio formateado', async () => {
    const el = await crear(base);

    expect(el.querySelector('.tarjeta__nombre')?.textContent).toContain('Mouse Logitech G203');
    expect(el.querySelector('.tarjeta__marca')?.textContent).toContain('Logitech');
    expect(el.querySelector('.tarjeta__categoria')?.textContent).toContain('Gaming');
    expect(el.querySelector('.tarjeta__precio')?.textContent).toContain('S/ 119.00');
  });

  it('sin imagenUrl muestra la inicial en lugar de la imagen', async () => {
    const el = await crear(base);

    expect(el.querySelector('img')).toBeNull();
    expect(el.querySelector('.tarjeta__inicial')?.textContent).toContain('M');
  });

  it('emite el producto al pulsar "Agregar al carrito"', async () => {
    const el = await crear(base);
    const emitidos: Producto[] = [];
    fixture.componentInstance.agregar.subscribe((p) => emitidos.push(p));

    el.querySelector<HTMLButtonElement>('.tarjeta__boton')?.click();

    expect(emitidos).toEqual([base]);
  });

  it('con stock 0 deshabilita el botón y avisa "Sin stock"', async () => {
    const el = await crear({ ...base, stock: 0 });

    expect(el.querySelector<HTMLButtonElement>('.tarjeta__boton')?.disabled).toBe(true);
    expect(el.querySelector('.tarjeta__stock')?.textContent).toContain('Sin stock');
  });

  it('con pocas unidades avisa cuántas quedan', async () => {
    const el = await crear({ ...base, stock: 3 });

    expect(el.querySelector('.tarjeta__stock')?.textContent).toContain('Últimas 3 unidades');
  });
});
