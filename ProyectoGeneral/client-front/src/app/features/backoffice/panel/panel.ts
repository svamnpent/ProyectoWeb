import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './panel.html',
  styleUrl: './panel.scss',
})
export class Panel {
  protected readonly auth = inject(AuthService);
  protected icono = true;
  protected readonly grupos = [
    { nombre: 'Catálogo', admin: false, enlaces: [
      { ruta: 'productos', nombre: 'Productos' }, { ruta: 'categorias', nombre: 'Categorías' },
      { ruta: 'etiquetas', nombre: 'Etiquetas' },
    ] },
    { nombre: 'Operaciones', admin: false, enlaces: [{ ruta: 'ventas', nombre: 'Ventas' }] },
    { nombre: 'Informes', admin: true, enlaces: [{ ruta: 'auditoria', nombre: 'Auditoría' }] },
    { nombre: 'Cuentas', admin: true, enlaces: [
      { ruta: 'usuarios', nombre: 'Usuarios' }, { ruta: 'perfiles', nombre: 'Perfiles' },
    ] },
  ];
}
