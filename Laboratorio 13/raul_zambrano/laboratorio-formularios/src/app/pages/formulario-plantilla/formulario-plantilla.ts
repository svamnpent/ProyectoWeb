import { JsonPipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';

interface Contacto {
  nombre: string;
  correo: string;
  tipo: string;
  mensaje: string;
  acepta: boolean;
}

function crearContactoVacio(): Contacto {
  return {
    nombre: '',
    correo: '',
    tipo: 'consulta',
    mensaje: '',
    acepta: false,
  };
}

@Component({
  selector: 'app-formulario-plantilla',
  imports: [FormsModule, JsonPipe],
  templateUrl: './formulario-plantilla.html',
  styleUrl: './formulario-plantilla.css',
})
export class FormularioPlantillaComponent {
  modelo: Contacto = crearContactoVacio();
  mensajeExito = '';

  enviar(formulario: NgForm): void {
    this.mensajeExito = '';

    if (formulario.invalid) {
      formulario.control.markAllAsTouched();
      return;
    }

    console.log('Datos del formulario basado en plantilla:', this.modelo);
    this.mensajeExito = `Solicitud enviada por ${this.modelo.nombre}.`;
  }

  limpiar(formulario: NgForm): void {
    this.modelo = crearContactoVacio();
    formulario.resetForm(this.modelo);
    this.mensajeExito = '';
  }
}
