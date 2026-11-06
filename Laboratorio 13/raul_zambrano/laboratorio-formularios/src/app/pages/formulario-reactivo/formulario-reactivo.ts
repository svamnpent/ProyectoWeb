import { JsonPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { edadMinimaValidator, passwordsIgualesValidator } from '../../validators/registro.validators';

@Component({
  selector: 'app-formulario-reactivo',
  imports: [ReactiveFormsModule, JsonPipe],
  templateUrl: './formulario-reactivo.html',
  styleUrl: './formulario-reactivo.css',
})
export class FormularioReactivoComponent {
  private readonly fb = inject(FormBuilder);

  mensajeExito = '';

  readonly registroForm = this.fb.group(
    {
      nombre: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(60),
      ]),
      correo: this.fb.nonNullable.control('', [Validators.required, Validators.email]),
      edad: this.fb.control<number | null>(null, [Validators.required, edadMinimaValidator(18)]),
      telefono: this.fb.nonNullable.control('', [Validators.pattern(/^\d{9}$/)]),
      password: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Z])(?=.*\d).+$/),
      ]),
      confirmarPassword: this.fb.nonNullable.control('', [Validators.required]),
      aceptaTerminos: this.fb.nonNullable.control(false, [Validators.requiredTrue]),
      habilidades: this.fb.array([this.crearControlHabilidad()]),
    },
    {
      validators: passwordsIgualesValidator,
    },
  );

  get habilidades(): FormArray {
    return this.registroForm.controls.habilidades;
  }

  private crearControlHabilidad() {
    return this.fb.nonNullable.control('', [Validators.required, Validators.minLength(2)]);
  }

  agregarHabilidad(): void {
    this.habilidades.push(this.crearControlHabilidad());
  }

  eliminarHabilidad(indice: number): void {
    if (this.habilidades.length > 1) {
      this.habilidades.removeAt(indice);
    }
  }

  enviar(): void {
    this.mensajeExito = '';

    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      return;
    }

    const datos = this.registroForm.getRawValue();
    console.log('Datos del formulario reactivo:', datos);
    this.mensajeExito = `Registro preparado para ${datos.nombre}.`;
  }

  limpiar(): void {
    this.registroForm.reset({
      nombre: '',
      correo: '',
      edad: null,
      telefono: '',
      password: '',
      confirmarPassword: '',
      aceptaTerminos: false,
      habilidades: [''],
    });

    while (this.habilidades.length > 1) {
      this.habilidades.removeAt(this.habilidades.length - 1);
    }

    this.habilidades.at(0).reset('');
    this.mensajeExito = '';
  }
}
