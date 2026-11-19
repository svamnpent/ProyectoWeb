import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { Perfil, Usuario } from '../../../core/models/usuario.model';
import { Solicitud } from '../solicitud';

@Component({ imports: [FormsModule], templateUrl: './cuenta.html', styleUrl: '../cliente.scss' })
export class Cuenta extends Solicitud {
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;
  protected usuario: Usuario | null = null;
  protected perfil: Perfil | null = null;
  protected avatar = '';
  protected biografia = '';
  protected vista: 'datos' | 'perfil' = 'datos';

  ngOnInit() {
    void this.cargar();
  }
  protected cargar() {
    return this.ejecutar(async () => {
      const id = this.auth.usuario()!.id;
      this.usuario = await firstValueFrom(this.http.get<Usuario>(this.api + '/usuarios/' + id));
      this.mostrarPerfil(this.usuario.perfil);
    });
  }
  private mostrarPerfil(p: Perfil | null) {
    this.perfil = p;
    this.avatar = p?.avatarUrl ?? '';
    this.biografia = p?.biografia ?? '';
  }
  protected abrirPerfil() {
    this.vista = 'perfil';
    if (this.perfil) {
      const id = this.perfil.id;
      void this.ejecutar(async () =>
        this.mostrarPerfil(
          await firstValueFrom(this.http.get<Perfil>(this.api + '/perfiles/' + id)),
        ),
      );
    }
  }
  protected guardarDatos(u: Usuario) {
    return this.ejecutar(async () => {
      const datos = {
        nombre: u.nombre,
        email: u.email,
        dni: u.dni || null,
        direccion: u.direccion || null,
        numeroTelefono: u.numeroTelefono || null,
      };
      this.usuario = { ...(await firstValueFrom(this.auth.actualizarCuenta(u.id, datos))) };
      this.aviso.set('Datos guardados.');
    });
  }
  protected guardarPerfil() {
    return this.ejecutar(async () => {
      const datos = {
        usuarioId: this.usuario!.id,
        biografia: this.biografia,
        avatarUrl: this.avatar,
      };
      const url = this.api + '/perfiles';
      const solicitud = this.perfil
        ? this.http.put<Perfil>(url + '/' + this.perfil.id, datos)
        : this.http.post<Perfil>(url, datos);
      this.mostrarPerfil(await firstValueFrom(solicitud));
      this.aviso.set('Perfil guardado.');
    });
  }
  protected eliminarPerfil() {
    const p = this.perfil;
    if (!p || !confirm('¿Eliminar el avatar y la biografía? Conservarás tu cuenta y tus pedidos.'))
      return;
    return this.ejecutar(async () => {
      await firstValueFrom(this.http.delete(this.api + '/perfiles/' + p.id));
      this.mostrarPerfil(null);
      this.aviso.set('Perfil eliminado.');
    });
  }
}
