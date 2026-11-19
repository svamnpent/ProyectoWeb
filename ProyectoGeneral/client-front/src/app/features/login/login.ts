import { Component, inject, input, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { LoginRequest } from '../../core/models/auth.model';
import { AuthService } from '../../core/services/auth.service';
import { mensajeDeError } from '../../core/utils/http-error';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Llega desde ?returnUrl=... gracias a withComponentInputBinding(). */
  readonly returnUrl = input<string>();

  protected readonly credenciales: LoginRequest = { email: '', password: '' };
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected iniciarSesion(formulario: NgForm): void {
    if (formulario.invalid || this.enviando()) {
      return;
    }
    this.enviando.set(true);
    this.error.set(null);

    this.auth
      .login(this.credenciales)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl(this.destino()),
        error: (e) => this.error.set(mensajeDeError(e, 'No se pudo iniciar sesión')),
      });
  }

  /** Solo se aceptan rutas internas (evita redirecciones a sitios externos). */
  private destino(): string {
    const url = this.returnUrl();
    return url?.startsWith('/') && !url.startsWith('//') ? url : '/';
  }
}
