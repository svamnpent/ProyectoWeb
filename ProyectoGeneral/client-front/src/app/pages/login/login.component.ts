import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="auth-card">
      <div class="header">
        <h2>Iniciar Sesión</h2>
        <p>Ingresa tus credenciales para acceder</p>
      </div>

      @if (errorMessage()) {
        <div class="alert-error">
          {{ errorMessage() }}
        </div>
      }

      <form (ngSubmit)="onSubmit()" class="auth-form">
        <div class="form-group">
          <label for="email">Correo Electrónico</label>
          <input
            id="email"
            type="email"
            [(ngModel)]="email"
            name="email"
            required
            placeholder="ejemplo@phantom.com"
          />
        </div>

        <div class="form-group">
          <label for="password">Contraseña</label>
          <input
            id="password"
            type="password"
            [(ngModel)]="password"
            name="password"
            required
            placeholder="••••••••"
          />
        </div>

        <button type="submit" class="btn-submit" [disabled]="loading()">
          {{ loading() ? 'Iniciando...' : 'Entrar' }}
        </button>
      </form>

      <div class="demo-box">
        <strong>Cuentas de Prueba:</strong>
        <p>🔑 <strong>Admin:</strong> admin&#64;phantom.com | admin123</p>
        <p>👤 <strong>User:</strong> juan&#64;phantom.com | user123</p>
      </div>
    </div>
  `,
  styles: [`
    .auth-card {
      max-width: 420px;
      margin: 3rem auto;
      background: #ffffff;
      padding: 2.2rem;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      font-family: inherit;
    }
    .header h2 {
      margin: 0 0 0.5rem;
      color: #1a1a24;
      font-size: 1.6rem;
    }
    .header p {
      margin: 0 0 1.5rem;
      color: #6c727f;
      font-size: 0.9rem;
    }
    .alert-error {
      background: #fee2e2;
      color: #dc2626;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      margin-bottom: 1.2rem;
      font-size: 0.88rem;
    }
    .form-group {
      display: flex;
      flex-direction: column;
      margin-bottom: 1.2rem;
      text-align: left;
    }
    .form-group label {
      margin-bottom: 0.4rem;
      font-weight: 500;
      font-size: 0.88rem;
      color: #374151;
    }
    .form-group input {
      padding: 0.65rem 0.85rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.2s;
    }
    .form-group input:focus {
      border-color: #6366f1;
    }
    .btn-submit {
      width: 100%;
      padding: 0.75rem;
      background: #4f46e5;
      color: #ffffff;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      font-size: 1rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    .btn-submit:hover:not(:disabled) {
      background: #4338ca;
    }
    .btn-submit:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
    .demo-box {
      margin-top: 1.8rem;
      padding: 0.85rem;
      background: #f8fafc;
      border-left: 4px solid #6366f1;
      border-radius: 4px;
      font-size: 0.82rem;
      color: #475569;
      text-align: left;
    }
    .demo-box p {
      margin: 0.3rem 0 0;
    }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    if (!this.email || !this.password) return;
    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err.error?.message || 'Error al iniciar sesión. Verifica tus credenciales.';
        this.errorMessage.set(msg);
      }
    });
  }
}

