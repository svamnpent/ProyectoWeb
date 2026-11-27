import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Productos } from './productos';

describe('Productos', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Productos],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should create and count favorites', () => {
    const fixture = TestBed.createComponent(Productos);
    const c = fixture.componentInstance;
    c.registrarFavorito(1);
    c.registrarFavorito(2);
    expect(c.favoritos()).toBe(2);
  });
});
