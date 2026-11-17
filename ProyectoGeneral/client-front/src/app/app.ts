import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { CarritoService } from './core/services/carrito.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly carrito = inject(CarritoService);
  private readonly router = inject(Router);
  protected readonly anio = new Date().getFullYear();

  protected cerrarSesion(): void {
    this.auth.cerrarSesion();
    void this.router.navigateByUrl('/');
  }
}
