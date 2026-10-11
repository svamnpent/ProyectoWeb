import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  template: `
    <header class="navbar">
      <div class="nav-container">
        <a routerLink="/" class="logo">
          ⚡ <span>Phantom Store</span>
        </a>
        <nav class="nav-links">
          <a routerLink="/" class="nav-link">Catálogo</a>
          
          @if (authService.isAuthenticated()) {
            <span class="user-badge">
              👤 {{ authService.currentUser()?.nombre }} ({{ authService.currentUser()?.rol }})
            </span>
            <button class="btn-logout" (click)="authService.logout()">Cerrar Sesión</button>
          } @else {
            <a routerLink="/login" class="btn-login">Iniciar Sesión</a>
          }
        </nav>
      </div>
    </header>

    <main class="main-content">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: #f9fafb;
      font-family: system-ui, -apple-system, sans-serif;
      color: #111827;
    }
    .navbar {
      background: #ffffff;
      border-bottom: 1px solid #e5e7eb;
      padding: 0.85rem 0;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .nav-container {
      max-width: 1080px;
      margin: 0 auto;
      padding: 0 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .logo {
      font-size: 1.25rem;
      font-weight: 700;
      text-decoration: none;
      color: #111827;
      display: flex;
      align-items: center;
      gap: 0.4rem;
    }
    .logo span {
      background: linear-gradient(135deg, #4f46e5, #7c3aed);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .nav-links {
      display: flex;
      align-items: center;
      gap: 1.2rem;
    }
    .nav-link {
      text-decoration: none;
      color: #4b5563;
      font-weight: 500;
      font-size: 0.95rem;
    }
    .nav-link:hover {
      color: #111827;
    }
    .user-badge {
      font-size: 0.88rem;
      color: #374151;
      background: #f3f4f6;
      padding: 0.35rem 0.75rem;
      border-radius: 20px;
      font-weight: 500;
    }
    .btn-login {
      text-decoration: none;
      background: #4f46e5;
      color: white;
      padding: 0.45rem 1rem;
      border-radius: 6px;
      font-weight: 500;
      font-size: 0.9rem;
    }
    .btn-login:hover {
      background: #4338ca;
    }
    .btn-logout {
      background: #f3f4f6;
      border: 1px solid #d1d5db;
      color: #374151;
      padding: 0.4rem 0.85rem;
      border-radius: 6px;
      font-size: 0.85rem;
      font-weight: 500;
      cursor: pointer;
    }
    .btn-logout:hover {
      background: #e5e7eb;
    }
    .main-content {
      padding: 1rem 0;
    }
  `]
})
export class App {
  constructor(public authService: AuthService) {}
}
