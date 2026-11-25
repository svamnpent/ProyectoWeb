import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormularioReactivoComponent } from './formulario-reactivo';

describe('FormularioReactivoComponent', () => {
  let fixture: ComponentFixture<FormularioReactivoComponent>;
  let c: FormularioReactivoComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioReactivoComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(FormularioReactivoComponent);
    c = fixture.componentInstance;
    await fixture.whenStable();
  });

  const llenarValido = () => {
    c.registroForm.patchValue({
      nombre: 'Ana Pérez',
      correo: 'ana@correo.com',
      edad: 25,
      telefono: '987654321',
      password: 'Abcdef12',
      confirmarPassword: 'Abcdef12',
      aceptaTerminos: true,
    });
    c.habilidades.at(0).setValue('Angular');
  };

  it('es inválido al inicio y marca todo como touched al enviar', () => {
    expect(c.registroForm.invalid).toBe(true);
    c.enviar();
    expect(c.registroForm.controls.nombre.touched).toBe(true);
  });

  it('activa edadMinima, pattern de teléfono y contraseñas distintas', () => {
    c.registroForm.controls.edad.setValue(15);
    expect(c.registroForm.controls.edad.hasError('edadMinima')).toBe(true);
    c.registroForm.controls.telefono.setValue('123');
    expect(c.registroForm.controls.telefono.hasError('pattern')).toBe(true);
    c.registroForm.patchValue({ password: 'Abcdef12', confirmarPassword: 'otra' });
    expect(c.registroForm.hasError('passwordsNoCoinciden')).toBe(true);
  });

  it('agrega y elimina habilidades sin bajar de una', () => {
    c.agregarHabilidad();
    c.agregarHabilidad();
    expect(c.habilidades.length).toBe(3);
    c.eliminarHabilidad(0);
    c.eliminarHabilidad(0);
    c.eliminarHabilidad(0);
    expect(c.habilidades.length).toBe(1);
  });

  it('es válido con datos correctos, envía y limpia', () => {
    llenarValido();
    expect(c.registroForm.valid).toBe(true);
    c.enviar();
    expect(c.mensajeExito).toContain('Ana Pérez');
    c.agregarHabilidad();
    c.limpiar();
    expect(c.habilidades.length).toBe(1);
    expect(c.registroForm.controls.nombre.value).toBe('');
    expect(c.mensajeExito).toBe('');
  });
});
