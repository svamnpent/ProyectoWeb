import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductoCard } from './producto-card';
import { PRODUCTOS } from '../../data/productos.mock';

describe('ProductoCard', () => {
  let fixture: ComponentFixture<ProductoCard>;
  let component: ProductoCard;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoCard],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductoCard);
    fixture.componentRef.setInput('producto', PRODUCTOS[0]);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit ids', () => {
    const detalle: number[] = [];
    const favoritos: number[] = [];
    component.verDetalle.subscribe(id => detalle.push(id));
    component.favorito.subscribe(id => favoritos.push(id));
    component.solicitarDetalle();
    component.marcarFavorito();
    expect(detalle).toEqual([1]);
    expect(favoritos).toEqual([1]);
  });
});
