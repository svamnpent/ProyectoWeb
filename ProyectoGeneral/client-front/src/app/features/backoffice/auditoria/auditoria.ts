import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, ElementRef, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Operaciones } from '../compartido/operaciones';
import { diaLima, Informe, Movimiento, resumen } from './informe';

@Component({
  imports: [FormsModule, CurrencyPipe, DatePipe],
  templateUrl: './auditoria.html',
  styleUrl: './auditoria.scss',
})
export class Auditoria extends Operaciones {
  protected vista: 'Resumen' | 'Movimientos' | 'Inventario' = 'Resumen';
  protected readonly vistas = ['Resumen', 'Movimientos', 'Inventario'] as const;
  protected hasta = diaLima(new Date());
  protected desde = this.hasta.slice(0, 8) + '01';
  protected readonly informe = signal<Informe | null>(null);
  protected readonly cifras = computed(() => {
    const datos = this.informe();
    return datos ? resumen(datos) : null;
  });
  protected readonly etiquetas: Record<Movimiento['tipo'], string> = {
    VENTA: 'Venta',
    HISTORICA: 'Venta histórica',
    AJUSTE: 'Ajuste de venta',
    ANULACION: 'Anulación',
    ALTA: 'Alta de producto',
    BAJA: 'Baja de producto',
    STOCK: 'Cambio de stock',
  };
  private readonly contenido = viewChild<ElementRef<HTMLElement>>('contenido');

  ngOnInit() {
    void this.consultar();
  }

  protected consultar() {
    return this.ejecutar(async () => {
      this.informe.set(null);
      const dias = (Date.parse(this.hasta) - Date.parse(this.desde)) / 86400000;
      if (!Number.isFinite(dias) || dias < 0 || dias > 92) {
        this.error.set('Selecciona un período de entre 1 y 93 días.');
        return;
      }
      this.informe.set(
        await firstValueFrom(
          this.http.get<Informe>(this.oficina + '/auditoria', {
            params: { desde: this.desde, hasta: this.hasta },
          }),
        ),
      );
    });
  }

  protected descargar() {
    const datos = this.informe(),
      elemento = this.contenido()?.nativeElement;
    if (!datos || !elemento) return;
    const doc = document.implementation.createHTMLDocument('PHANTOM · Auditoría comercial');
    doc.documentElement.lang = 'es';
    const meta = doc.createElement('meta');
    meta.setAttribute('charset', 'UTF-8');
    doc.head.prepend(meta);
    const estilo = doc.createElement('style');
    estilo.textContent = `
      body{font:15px system-ui;color:#211b30;max-width:1100px;margin:2rem auto;padding:0 1rem}
      table{width:100%;border-collapse:collapse}td,th{text-align:left;padding:.45rem;border-bottom:1px solid #ddd}
      h2,h3{break-after:avoid}.barra{display:block;background:#684a9e;height:.5rem;border-bottom:4px solid #684a9e}
      .negativa{background:#b53437;border-color:#b53437}dl{display:flex;gap:1.5rem;flex-wrap:wrap}dd{margin:0;font-weight:bold}
      @media print{tr{break-inside:avoid}thead{display:table-header-group}}
    `;
    doc.head.append(estilo);
    doc.body.append(doc.importNode(elemento, true));
    const archivo = new Blob(['<!doctype html>', doc.documentElement.outerHTML], {
      type: 'text/html;charset=utf-8',
    });
    const url = URL.createObjectURL(archivo);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'Phantom-auditoria-' + datos.desde + '_' + datos.hasta + '.html';
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  protected imprimir() {
    window.print();
  }
}
