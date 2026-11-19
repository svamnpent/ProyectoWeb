import { Component, inject, input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { Solicitud } from '../solicitud';

@Component({
  imports: [FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: '../cliente.scss',
})
export class Registro extends Solicitud {
  readonly returnUrl = input<string>();
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected datos = { nombre: '', email: '', password: '' };
  protected confirmar = '';
  protected registrar() {
    if (this.datos.password !== this.confirmar) return;
    return this.ejecutar(async () => {
      await firstValueFrom(this.auth.registrar(this.datos));
      this.datos.password = this.confirmar = '';
      const destino = this.returnUrl();
      await this.router.navigateByUrl(
        destino?.startsWith('/') && !destino.startsWith('//') ? destino : '/',
      );
    });
  }
}
