import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Etiqueta, EtiquetaRequest } from '../../../../core/models/catalogo.model';
import { Mantenimiento } from '../../compartido/operaciones';

@Component({
  imports: [FormsModule],
  templateUrl: './etiquetas.html',
  styleUrl: '../../compartido/pagina.scss',
})
export class Etiquetas extends Mantenimiento<EtiquetaRequest, Etiqueta> {
  protected readonly recurso = 'etiquetas';

  ngOnInit() {
    void this.ejecutar(() => this.cargar());
  }
  protected nuevo(): EtiquetaRequest {
    return { nombre: '' };
  }
  protected copiar(fila: Etiqueta): EtiquetaRequest {
    return { nombre: fila.nombre };
  }
  protected async cargar() {
    this.filas = await firstValueFrom(this.http.get<Etiqueta[]>(this.api + '/etiquetas'));
  }
  protected recargar() {
    return this.ejecutar(() => this.cargar());
  }
}
