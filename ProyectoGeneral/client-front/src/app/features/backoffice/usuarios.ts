import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Usuario } from '../../core/models/usuario.model';
import { Rol } from '../../core/models/rol';
import { Mantenimiento } from './operaciones';

interface UsuarioRequest {
  nombre: string;
  email: string;
  password: string;
  rol: Rol;
  dni?: string;
  direccion?: string;
  numeroTelefono?: string;
}

@Component({
  imports: [FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './pagina.scss',
})
export class Usuarios extends Mantenimiento<UsuarioRequest, Usuario> {
  protected readonly recurso = 'usuarios';
  protected correo = '';
  protected buscar() {
    return this.ejecutar(async () => {
      this.abierto = false;
      this.filas = [];
      this.filas = [
        await firstValueFrom(
          this.http.get<Usuario>(this.oficina + '/usuarios/buscar', {
            params: { email: this.correo.trim() },
          }),
        ),
      ];
    });
  }
  protected abrirUsuario(id: number) {
    return this.ejecutar(async () =>
      this.editar(await firstValueFrom(this.http.get<Usuario>(this.oficina + '/usuarios/' + id))),
    );
  }
  protected override datos(): UsuarioRequest {
    const f = this.formulario;
    return { ...f, dni: f.dni || undefined, numeroTelefono: f.numeroTelefono || undefined };
  }
  ngOnInit() {
    void this.ejecutar(() => this.cargar());
  }
  protected nuevo(): UsuarioRequest {
    return { nombre: '', email: '', password: '', rol: 'USER' };
  }
  protected copiar(fila: Usuario): UsuarioRequest {
    return {
      nombre: fila.nombre,
      email: fila.email,
      password: '',
      rol: fila.rol,
      dni: fila.dni ?? undefined,
      direccion: fila.direccion ?? undefined,
      numeroTelefono: fila.numeroTelefono ?? undefined,
    };
  }
  protected async cargar() {
    this.correo = '';
    this.filas = await firstValueFrom(this.http.get<Usuario[]>(this.oficina + '/usuarios'));
  }
  protected recargar() {
    return this.ejecutar(() => this.cargar());
  }
}
