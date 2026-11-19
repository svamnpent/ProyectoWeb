import { Component } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Perfil } from '../../../../core/models/usuario.model';
import { Operaciones } from '../../compartido/operaciones';

@Component({
  template: ` <h2>Perfiles</h2>
    <button (click)="cargar()" [disabled]="ocupado()">Actualizar</button>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
    }
    @if (ocupado()) {
      <p role="status">Cargando perfiles…</p>
    }
    @for (perfil of perfiles; track perfil.id) {
      <article>
        <h3>Usuario #{{ perfil.usuarioId }}</h3>
        <p>{{ perfil.biografia || 'Sin biografía' }}</p>
        @if (perfil.avatarUrl) {
          <p>Avatar · {{ perfil.avatarUrl }}</p>
        }
      </article>
    } @empty {
      @if (!ocupado() && !error()) {
        <p>No hay perfiles.</p>
      }
    }`,
  styleUrl: '../../compartido/pagina.scss',
})
export class Perfiles extends Operaciones {
  protected perfiles: Perfil[] = [];
  ngOnInit() {
    void this.cargar();
  }
  protected cargar() {
    return this.ejecutar(async () => {
      this.perfiles = await firstValueFrom(this.http.get<Perfil[]>(this.oficina + '/perfiles'));
    });
  }
}
