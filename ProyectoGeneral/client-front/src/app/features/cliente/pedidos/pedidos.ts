import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Venta, DetalleVenta } from '../../../core/models/venta.model';
import { VentaService } from '../../../core/services/venta.service';
import { Solicitud } from '../solicitud';

@Component({
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './pedidos.html',
  styleUrl: '../cliente.scss',
})
export class Pedidos extends Solicitud {
  private readonly servicio = inject(VentaService);
  protected ventas: Venta[] = [];
  protected seleccion: number | null = null;
  protected lineas: DetalleVenta[] = [];
  ngOnInit() {
    void this.cargar();
  }
  protected cargar() {
    return this.ejecutar(async () => {
      this.seleccion = null;
      this.ventas = await firstValueFrom(this.servicio.mias());
    });
  }
  protected consultarLineas(id: number) {
    if (this.seleccion === id) {
      this.seleccion = null;
      return;
    }
    return this.ejecutar(async () => {
      this.seleccion = null;
      this.lineas = await firstValueFrom(this.servicio.detalles(id));
      this.seleccion = id;
    });
  }
}
