import { FormControl, FormGroup } from '@angular/forms';
import { edadMinimaValidator, passwordsIgualesValidator } from './registro.validators';

describe('registro.validators', () => {
  it('edadMinima: rechaza menores de 18 y acepta 18+', () => {
    const validar = edadMinimaValidator(18);
    expect(validar(new FormControl(17))).toEqual({ edadMinima: { minima: 18, actual: 17 } });
    expect(validar(new FormControl(18))).toBeNull();
    expect(validar(new FormControl(null))).toBeNull();
  });

  it('passwordsIguales: detecta contraseñas distintas', () => {
    const grupo = new FormGroup({
      password: new FormControl('Abcdef12'),
      confirmarPassword: new FormControl('Abcdef13'),
    });
    expect(passwordsIgualesValidator(grupo)).toEqual({ passwordsNoCoinciden: true });
    grupo.get('confirmarPassword')!.setValue('Abcdef12');
    expect(passwordsIgualesValidator(grupo)).toBeNull();
  });
});
