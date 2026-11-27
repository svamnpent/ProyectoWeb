import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { SesionService } from './core/sesion.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly sesion = inject(SesionService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected salir(): void {
    this.auth.cerrarSesion();
    void this.router.navigateByUrl('/login');
  }
}
