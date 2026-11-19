import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: ` <div class="contenedor panel">
    <h1>
      @if (icono) {
        <img src="phantom-icon.svg" alt="" (error)="icono = false" />
      }
      Back-office
    </h1>
    <nav aria-label="Operaciones de tienda">
      @for (item of menu; track item[0]) {
        <a [routerLink]="item[0]" routerLinkActive="actual" ariaCurrentWhenActive="page">{{
          item[1]
        }}</a>
      }
      @if (auth.esAdmin()) {
        <a routerLink="usuarios" routerLinkActive="actual" ariaCurrentWhenActive="page">Usuarios</a>
        <a routerLink="perfiles" routerLinkActive="actual" ariaCurrentWhenActive="page">Perfiles</a>
      }
    </nav>
    <router-outlet />
  </div>`,
  styles: `
    .panel {
      padding-block: 2rem;
    }
    h1 {
      display: flex;
      align-items: center;
      gap: 0.65rem;
    }
    img {
      width: 2rem;
      height: 2rem;
      object-fit: contain;
    }
    nav {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      margin: 1rem 0 1.5rem;
    }
    a {
      padding: 0.45rem 0.85rem;
      border-radius: 0.4rem;
      background: #fff;
      color: #1a0b3d;
      border: 1px solid #ddd;
    }
    .actual {
      color: white;
      background: #1a0b3d;
    }
  `,
})
export class Panel {
  protected readonly auth = inject(AuthService);
  protected icono = true;
  protected readonly menu = [
    ['productos', 'Productos'],
    ['categorias', 'Categorías'],
    ['etiquetas', 'Etiquetas'],
    ['ventas', 'Ventas'],
  ];
}
