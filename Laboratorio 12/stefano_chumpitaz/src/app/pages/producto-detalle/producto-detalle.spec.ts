import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ProductoDetalle } from './producto-detalle';

describe('ProductoDetalle', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoDetalle],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(ProductoDetalle);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
