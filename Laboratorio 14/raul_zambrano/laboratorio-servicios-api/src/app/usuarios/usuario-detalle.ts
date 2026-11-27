import { Component, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Usuario } from './usuario.model';
import { UsuarioService } from './usuario.service';

@Component({
  selector: 'app-usuario-detalle',
  imports: [RouterLink],
  templateUrl: './usuario-detalle.html',
  styleUrl: './usuarios.css',
})
export class UsuarioDetalle implements OnInit {
  private readonly usuarioService = inject(UsuarioService);

  readonly id = input.required<string>();
  protected readonly usuario = signal<Usuario | null>(null);
  protected readonly error = signal('');
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.consultar();
  }

  private consultar(): void {
    const numero = Number(this.id());
    if (!Number.isInteger(numero) || numero < 1) {
      this.error.set('El identificador no es válido.');
      this.cargando.set(false);
      return;
    }
    this.usuarioService.obtenerPorId(numero).subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo consultar el usuario.');
        this.cargando.set(false);
      },
    });
  }
}
