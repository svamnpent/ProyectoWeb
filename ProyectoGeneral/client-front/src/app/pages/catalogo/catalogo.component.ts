import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TiendaService } from '../../services/tienda.service';
import { AuthService } from '../../services/auth.service';
import { Producto, Categoria } from '../../models/models';

@Component({
  selector: 'app-catalogo',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="catalogo-container">
      <div class="top-bar">
        <h2>Catálogo de Productos</h2>
        <div class="search-box">
          <input
            type="text"
            [(ngModel)]="busqueda"
            (keyup.enter)="buscar()"
            placeholder="Buscar por nombre o marca..."
          />
          <button (click)="buscar()">Buscar</button>
        </div>
      </div>

      @if (mensajeVenta()) {
        <div class="alert-success">
          {{ mensajeVenta() }}
        </div>
      }

      @if (errorVenta()) {
        <div class="alert-error">
          {{ errorVenta() }}
        </div>
      }

      @if (cargando()) {
        <p class="loading-text">Cargando productos...</p>
      } @else if (productos().length === 0) {
        <p class="empty-text">No se encontraron productos disponibles.</p>
      } @else {
        <div class="grid-productos">
          @for (prod of productos(); track prod.id) {
            <div class="card-producto">
              <div class="badge-cat">{{ prod.categoria.nombre }}</div>
              <h3>{{ prod.nombre }}</h3>
              <p class="desc">{{ prod.descripcion || 'Sin descripción detallada.' }}</p>
              
              <div class="meta">
                <span class="precio">S/ {{ prod.precio.toFixed(2) }}</span>
                <span class="stock" [class.agotado]="prod.stock <= 0">
                  Stock: {{ prod.stock }} unid.
                </span>
              </div>

              @if (prod.etiquetas && prod.etiquetas.length > 0) {
                <div class="tags">
                  @for (tag of prod.etiquetas; track tag.id) {
                    <span class="tag">#{{ tag.nombre }}</span>
                  }
                </div>
              }

              <button
                class="btn-comprar"
                [disabled]="prod.stock <= 0 || comprandoId() === prod.id"
                (click)="comprar(prod)"
              >
                {{ comprandoId() === prod.id ? 'Procesando...' : (prod.stock > 0 ? 'Comprar 1 Unidad' : 'Agotado') }}
              </button>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .catalogo-container {
      max-width: 1080px;
      margin: 2rem auto;
      padding: 0 1rem;
    }
    .top-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .top-bar h2 {
      margin: 0;
      color: #111827;
    }
    .search-box {
      display: flex;
      gap: 0.5rem;
    }
    .search-box input {
      padding: 0.5rem 0.8rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-size: 0.9rem;
      width: 240px;
    }
    .search-box button {
      padding: 0.5rem 1rem;
      background: #4f46e5;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 500;
    }
    .alert-success {
      background: #dcfce7;
      color: #15803d;
      padding: 0.8rem 1rem;
      border-radius: 6px;
      margin-bottom: 1.2rem;
    }
    .alert-error {
      background: #fee2e2;
      color: #b91c1c;
      padding: 0.8rem 1rem;
      border-radius: 6px;
      margin-bottom: 1.2rem;
    }
    .grid-productos {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
    }
    .card-producto {
      background: white;
      border: 1px solid #e5e7eb;
      border-radius: 10px;
      padding: 1.2rem;
      display: flex;
      flex-direction: column;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    }
    .badge-cat {
      align-self: flex-start;
      background: #e0e7ff;
      color: #4338ca;
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.2rem 0.6rem;
      border-radius: 20px;
      margin-bottom: 0.5rem;
    }
    .card-producto h3 {
      margin: 0 0 0.5rem;
      font-size: 1.1rem;
      color: #1f2937;
    }
    .desc {
      font-size: 0.85rem;
      color: #6b7280;
      flex-grow: 1;
      margin-bottom: 1rem;
    }
    .meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.8rem;
    }
    .precio {
      font-size: 1.3rem;
      font-weight: 700;
      color: #111827;
    }
    .stock {
      font-size: 0.85rem;
      color: #059669;
      font-weight: 500;
    }
    .stock.agotado {
      color: #dc2626;
    }
    .tags {
      display: flex;
      gap: 0.4rem;
      flex-wrap: wrap;
      margin-bottom: 1rem;
    }
    .tag {
      background: #f3f4f6;
      color: #4b5563;
      font-size: 0.75rem;
      padding: 0.15rem 0.45rem;
      border-radius: 4px;
    }
    .btn-comprar {
      padding: 0.65rem;
      background: #10b981;
      color: white;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }
    .btn-comprar:hover:not(:disabled) {
      background: #059669;
    }
    .btn-comprar:disabled {
      background: #9ca3af;
      cursor: not-allowed;
    }
  `]
})
export class CatalogoComponent implements OnInit {
  productos = signal<Producto[]>([]);
  cargando = signal<boolean>(true);
  busqueda = '';
  mensajeVenta = signal<string | null>(null);
  errorVenta = signal<string | null>(null);
  comprandoId = signal<number | null>(null);

  constructor(
    private tiendaService: TiendaService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.tiendaService.getProductos().subscribe({
      next: (prods) => {
        this.productos.set(prods);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  buscar(): void {
    if (!this.busqueda.trim()) {
      this.cargar();
      return;
    }
    this.cargando.set(true);
    this.tiendaService.buscarProductos(this.busqueda).subscribe({
      next: (prods) => {
        this.productos.set(prods);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  comprar(producto: Producto): void {
    if (!this.authService.isAuthenticated()) {
      this.errorVenta.set('Debes iniciar sesión con tu cuenta para realizar una compra.');
      return;
    }

    this.mensajeVenta.set(null);
    this.errorVenta.set(null);
    this.comprandoId.set(producto.id);

    const usuario = this.authService.currentUser();
    this.tiendaService.crearVenta({
      usuarioId: usuario?.id,
      detalles: [{ productoId: producto.id, cantidad: 1 }]
    }).subscribe({
      next: (venta) => {
        this.comprandoId.set(null);
        this.mensajeVenta.set(`¡Compra realizada con éxito! Orden: ${venta.numeroOrden} por S/ ${venta.total.toFixed(2)}`);
        this.cargar(); // Refrescar el stock actualizado en la base de datos
      },
      error: (err) => {
        this.comprandoId.set(null);
        this.errorVenta.set(err.error?.message || 'Error al procesar la venta. Verifique existencias.');
      }
    });
  }
}

