import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Producto } from '../../../core/models/catalogo.model';
import {
  DetalleVenta,
  DetalleVentaRequest,
  Venta,
  VentaRequest,
} from '../../../core/models/venta.model';
import { Operaciones } from '../compartido/operaciones';

@Component({
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './ventas.html',
  styleUrl: '../compartido/pagina.scss',
})
export class Ventas extends Operaciones {
  private readonly url = this.oficina + '/ventas';
  protected ventas: Venta[] = [];
  protected productos: Producto[] = [];
  protected seleccion: Venta | null = null;
  protected lineas: DetalleVenta[] = [];
  protected mostrador = false;
  protected cliente: number | null = null;
  protected borrador: DetalleVentaRequest[] = [];
  protected producto: number | null = null;
  protected cantidad: number | null = 1;

  ngOnInit() {
    void this.actualizar();
  }

  private async cargar() {
    [this.ventas, this.productos] = await Promise.all([
      firstValueFrom(this.http.get<Venta[]>(this.url)),
      firstValueFrom(this.http.get<Producto[]>(this.api + '/productos')),
    ]);
  }

  protected actualizar() {
    return this.ejecutar(async () => {
      await this.cargar();
      if (this.seleccion) await this.detalle(this.seleccion.id);
    });
  }

  private limpiarLinea() {
    this.producto = null;
    this.cantidad = 1;
  }

  protected nueva() {
    this.seleccion = null;
    this.mostrador = true;
    this.cliente = null;
    this.borrador = [];
    this.limpiarLinea();
  }

  protected nombre(id: number) {
    return this.productos.find((p) => p.id === id)?.nombre ?? '#' + id;
  }

  protected agregar() {
    if (!this.producto || !this.cantidad || !Number.isInteger(this.cantidad) || this.cantidad < 1)
      return;
    const linea = { productoId: this.producto, cantidad: this.cantidad };
    if (this.mostrador) {
      const existente = this.borrador.find((d) => d.productoId === linea.productoId);
      if (existente) existente.cantidad += linea.cantidad;
      else this.borrador.push(linea);
      this.limpiarLinea();
    } else if (this.seleccion && this.auth.esAdmin()) {
      const id = this.seleccion.id;
      void this.ejecutar(async () => {
        await firstValueFrom(this.http.post(this.url + '/' + id + '/detalles', linea));
        this.limpiarLinea();
        this.aviso.set('Línea agregada.');
        await this.refrescarVenta(id);
      });
    }
  }

  protected registrar() {
    if (!this.borrador.length || !this.mostrador) return;
    const solicitud: VentaRequest = {
      usuarioId: this.cliente ?? undefined,
      detalles: this.borrador,
    };
    return this.ejecutar(async () => {
      const venta = await firstValueFrom(this.http.post<Venta>(this.url, solicitud));
      this.mostrador = false;
      this.borrador = [];
      this.aviso.set('Venta registrada.');
      await this.refrescarVenta(venta.id);
    });
  }

  private async detalle(id: number) {
    const [venta, lineas] = await Promise.all([
      firstValueFrom(this.http.get<Venta>(this.url + '/' + id)),
      firstValueFrom(this.http.get<DetalleVenta[]>(this.url + '/' + id + '/detalles')),
    ]);
    this.seleccion = venta;
    this.lineas = lineas;
  }

  private async refrescarVenta(id: number) {
    this.seleccion = null;
    await this.cargar();
    await this.detalle(id);
  }

  protected ver(id: number) {
    return this.ejecutar(async () => {
      this.mostrador = false;
      this.seleccion = null;
      this.limpiarLinea();
      await this.detalle(id);
    });
  }

  protected quitar(linea: DetalleVenta) {
    if (
      !this.seleccion ||
      !this.auth.esAdmin() ||
      !confirm('¿Quitar ' + linea.productoNombre + ' de la venta?')
    )
      return;
    const id = this.seleccion.id;
    return this.ejecutar(async () => {
      await firstValueFrom(this.http.delete(this.oficina + '/detalles/' + linea.id));
      this.aviso.set('Línea eliminada.');
      await this.refrescarVenta(id);
    });
  }

  protected anular(venta: Venta) {
    if (!this.auth.esAdmin() || !confirm('¿Anular la venta #' + venta.id + ' y reponer el stock?'))
      return;
    return this.ejecutar(async () => {
      await firstValueFrom(this.http.delete(this.url + '/' + venta.id));
      this.seleccion = null;
      this.aviso.set('Venta anulada.');
      await this.cargar();
    });
  }
}
