import { CurrencyPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, combineLatest, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Categoria, Etiqueta, Producto } from '../../../core/models/catalogo.model';
import { mensajeDeError } from '../../../core/utils/http-error';

interface Resultados {
  titulo: string;
  descripcion?: string | null;
  productos: Producto[];
  categorias: Categoria[];
}
@Component({
  imports: [FormsModule, RouterLink, CurrencyPipe],
  templateUrl: './explorar.html',
  styleUrl: '../cliente.scss',
})
export class Explorar {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = environment.apiUrl;
  protected readonly resultado = signal<Resultados | null>(null);
  protected readonly error = signal('');
  protected texto = '';
  protected terminoConsultado = '';
  protected pagina = 1;
  protected buscando = this.route.snapshot.data['tipo'] === 'buscar';

  constructor() {
    combineLatest([this.route.paramMap, this.route.queryParamMap])
      .pipe(
        switchMap(([params, query]) => {
          this.error.set('');
          this.resultado.set(null);
          this.pagina = 1;
          this.texto = query.get('q')?.trim() ?? '';
          this.terminoConsultado = this.texto;
          const tipo = this.route.snapshot.data['tipo'];
          return this.consulta(tipo, Number(params.get('id')), this.texto).pipe(
            catchError((e) => {
              this.error.set(mensajeDeError(e, 'No se pudo consultar el catálogo.'));
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((r) => this.resultado.set(r));
  }
  protected buscar() {
    void this.router.navigate(['/buscar'], { queryParams: { q: this.texto.trim() || null } });
  }
  private consulta(tipo: string, id: number, keyword: string): Observable<Resultados> {
    if (tipo === 'buscar') {
      const titulo = keyword ? 'Resultados para «' + keyword + '»' : 'Buscar en el catálogo';
      if (!keyword) return of({ titulo, productos: [], categorias: [] });
      const params = { keyword };
      return forkJoin({
        productos: this.http.get<Producto[]>(this.api + '/productos/buscar', { params }),
        categorias: this.http.get<Categoria[]>(this.api + '/categorias/buscar', { params }),
      }).pipe(map((r) => ({ ...r, titulo })));
    }
    const esCategoria = tipo === 'categoria';
    const ficha = esCategoria
      ? this.http.get<Categoria>(this.api + '/categorias/' + id)
      : this.http.get<Etiqueta>(this.api + '/etiquetas/' + id);
    return forkJoin({ ficha, productos: this.http.get<Producto[]>(this.api + '/productos') }).pipe(
      map(({ ficha, productos }) => ({
        titulo: ficha.nombre,
        descripcion: 'descripcion' in ficha ? (ficha as Categoria).descripcion : null,
        productos: productos.filter((p) =>
          esCategoria ? p.categoria.id === id : p.etiquetas.some((e) => e.id === id),
        ),
        categorias: [],
      })),
    );
  }
}
