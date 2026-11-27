import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  templateUrl: './app-footer.html',
  styleUrl: './app-footer.scss'
})
export class AppFooter {
  protected readonly curso = 'Desarrollo Web Integrado';
  protected readonly anio = new Date().getFullYear();
}
