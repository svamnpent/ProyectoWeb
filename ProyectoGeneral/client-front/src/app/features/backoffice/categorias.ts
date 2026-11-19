import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Categoria, CategoriaRequest } from '../../core/models/catalogo.model';
import { Mantenimiento } from './operaciones';

@Component({
  imports: [FormsModule],
  templateUrl: './categorias.html',
  styleUrl: './pagina.scss',
})
export class Categorias extends Mantenimiento<CategoriaRequest, Categoria> {
  protected readonly recurso = 'categorias';

  ngOnInit() {
    void this.ejecutar(() => this.cargar());
  }
  protected nuevo(): CategoriaRequest {
    return { nombre: '' };
  }
  protected copiar(fila: Categoria): CategoriaRequest {
    return { nombre: fila.nombre, descripcion: fila.descripcion ?? undefined };
  }
  protected async cargar() {
    this.filas = await firstValueFrom(this.http.get<Categoria[]>(this.api + '/categorias'));
  }
  protected recargar() {
    return this.ejecutar(() => this.cargar());
  }
}
