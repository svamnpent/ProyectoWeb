import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function edadMinimaValidator(minima: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value;

    // Validators.required será responsable del valor vacío.
    if (valor === null || valor === '') {
      return null;
    }

    const edad = Number(valor);

    if (Number.isNaN(edad) || edad < minima) {
      return {
        edadMinima: {
          minima,
          actual: valor,
        },
      };
    }

    return null;
  };
}

export const passwordsIgualesValidator: ValidatorFn = (
  grupo: AbstractControl,
): ValidationErrors | null => {
  const password = grupo.get('password')?.value;
  const confirmar = grupo.get('confirmarPassword')?.value;

  if (!password || !confirmar) {
    return null;
  }

  return password === confirmar ? null : { passwordsNoCoinciden: true };
};
