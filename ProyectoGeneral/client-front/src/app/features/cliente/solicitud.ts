import { PendingTasks, inject, signal } from '@angular/core';
import { mensajeDeError } from '../../core/utils/http-error';

export abstract class Solicitud {
  private readonly pendientes = inject(PendingTasks);
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly aviso = signal('');

  protected async ejecutar(accion: () => Promise<void>) {
    if (this.cargando()) return;
    const finalizar = this.pendientes.add();
    this.cargando.set(true);
    this.error.set('');
    this.aviso.set('');
    try {
      await accion();
    } catch (e) {
      this.error.set(mensajeDeError(e));
    } finally {
      this.cargando.set(false);
      finalizar();
    }
  }
}
