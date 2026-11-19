import { HttpClient } from '@angular/common/http';
import { inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { mensajeDeError } from '../../core/utils/http-error';

export abstract class Operaciones {
  protected readonly http = inject(HttpClient);
  protected readonly auth = inject(AuthService);
  protected readonly api = environment.apiUrl;
  protected readonly oficina = this.api + '/backoffice';
  protected readonly ocupado = signal(false);
  protected readonly error = signal('');
  protected readonly aviso = signal('');

  protected async ejecutar(tarea: () => Promise<void>) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.error.set('');
    this.aviso.set('');
    try {
      await tarea();
    } catch (e) {
      this.error.set(mensajeDeError(e, 'No se pudo completar la operación.'));
    } finally {
      this.ocupado.set(false);
    }
  }
}

export abstract class Mantenimiento<
  F,
  T extends { id: number; nombre: string },
> extends Operaciones {
  protected abstract readonly recurso: string;
  protected abstract nuevo(): F;
  protected abstract copiar(fila: T): F;
  protected abstract cargar(): Promise<void>;
  protected filas: T[] = [];
  protected formulario!: F;
  protected id: number | null = null;
  protected abierto = false;

  protected editar(fila?: T) {
    this.id = fila?.id ?? null;
    this.formulario = fila ? this.copiar(fila) : this.nuevo();
    this.abierto = true;
  }

  protected datos(): F {
    return this.formulario;
  }

  protected guardar() {
    return this.ejecutar(async () => {
      const url = this.oficina + '/' + this.recurso;
      const datos = this.datos();
      await firstValueFrom(
        this.id === null
          ? this.http.post<T>(url, datos)
          : this.http.put<T>(url + '/' + this.id, datos),
      );
      this.abierto = false;
      this.aviso.set('Cambios guardados.');
      await this.cargar();
    });
  }

  protected borrar(fila: T) {
    if (!this.auth.esAdmin() || !confirm('¿Eliminar «' + fila.nombre + '»?')) return;
    return this.ejecutar(async () => {
      await firstValueFrom(this.http.delete(this.oficina + '/' + this.recurso + '/' + fila.id));
      this.abierto = false;
      this.aviso.set('Registro eliminado.');
      await this.cargar();
    });
  }
}
