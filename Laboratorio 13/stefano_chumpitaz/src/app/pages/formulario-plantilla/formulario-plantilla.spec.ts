import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormularioPlantillaComponent } from './formulario-plantilla';

describe('FormularioPlantillaComponent', () => {
  let fixture: ComponentFixture<FormularioPlantillaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioPlantillaComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(FormularioPlantillaComponent);
    await fixture.whenStable();
  });

  it('muestra errores al enviar vacío y no muestra éxito', async () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelectorAll('.error').length).toBe(0);
    el.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
    expect(el.querySelectorAll('.error').length).toBeGreaterThan(0);
    expect(el.querySelector('.exito')).toBeNull();
  });

  it('envía con datos válidos y muestra éxito', async () => {
    const el = fixture.nativeElement as HTMLElement;
    const set = (sel: string, v: string) => {
      const i = el.querySelector<HTMLInputElement | HTMLTextAreaElement>(sel)!;
      i.value = v;
      i.dispatchEvent(new Event('input'));
    };
    set('#nombrePlantilla', 'Luis Gómez');
    set('#correoPlantilla', 'luis@correo.com');
    set('#mensaje', 'Mensaje de prueba largo');
    const chk = el.querySelector<HTMLInputElement>('#aceptaPlantilla')!;
    chk.click();
    await fixture.whenStable();
    el.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
    expect(el.querySelector('.exito')?.textContent).toContain('Luis Gómez');
  });
});
