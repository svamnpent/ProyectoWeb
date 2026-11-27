import { Component, inject, OnInit, signal } from '@angular/core';
import { AdminService } from '../../core/admin.service';
import { mensajeHttp } from '../../core/http-error';
import { ResumenAdmin } from '../../models/modelos';

@Component({
  selector: 'app-admin-resumen',
  templateUrl: './admin-resumen.html',
  styleUrl: './admin-resumen.css',
})
export class AdminResumen implements OnInit {
  private readonly api = inject(AdminService);
  protected readonly resumen = signal<ResumenAdmin | null>(null);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.api.resumen().subscribe({
      next: (datos) => this.resumen.set(datos),
      error: (error) => this.error.set(mensajeHttp(error)),
    });
  }
}
