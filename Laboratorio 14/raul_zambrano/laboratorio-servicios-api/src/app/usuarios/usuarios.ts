import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Usuario, UsuarioEntrada } from './usuario.model';
import { UsuarioService } from './usuario.service';

@Component({
  selector: 'app-usuarios',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly fb = inject(FormBuilder);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly error = signal('');
  protected readonly mensaje = signal('');
  protected readonly exitosas = signal(0);
  protected readonly usuarioEditandoId = signal<number | null>(null);

  protected readonly visibles = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const lista = this.usuarios();
    if (!texto) {
      return lista;
    }
    return lista.filter(
      (usuario) =>
        usuario.name.toLowerCase().includes(texto) || usuario.email.toLowerCase().includes(texto),
    );
  });

  protected readonly formulario = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.minLength(7)]],
    website: [''],
  });

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  protected cargarUsuarios(): void {
    this.cargando.set(true);
    this.error.set('');
    this.mensaje.set('');
    this.usuarioService
      .obtenerTodos()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (usuarios) => {
          this.usuarios.set(usuarios);
          this.exitosas.update((total) => total + 1);
        },
        error: (error) => {
          console.error('Error al obtener usuarios:', error);
          this.error.set('No se pudo obtener la lista de usuarios.');
        },
      });
  }

  protected guardar(): void {
    this.error.set('');
    this.mensaje.set('');
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.error.set('Revise los campos obligatorios del formulario.');
      return;
    }
    const datos: UsuarioEntrada = this.formulario.getRawValue();
    const id = this.usuarioEditandoId();
    this.guardando.set(true);
    const solicitud$ =
      id === null ? this.usuarioService.crear(datos) : this.usuarioService.actualizar(id, datos);
    solicitud$.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: (respuesta) => {
        if (id === null) {
          this.usuarios.update((actuales) => [respuesta, ...actuales]);
          this.mensaje.set(`Usuario creado. ID recibido: ${respuesta.id}.`);
        } else {
          const actualizado: Usuario = { ...respuesta, id };
          this.usuarios.update((actuales) =>
            actuales.map((usuario) => (usuario.id === id ? { ...usuario, ...actualizado } : usuario)),
          );
          this.mensaje.set('Usuario actualizado correctamente.');
        }
        this.exitosas.update((total) => total + 1);
        this.cancelarEdicion();
      },
      error: (error) => {
        console.error('Error al guardar:', error);
        this.error.set('La operación no pudo completarse.');
      },
    });
  }

  protected editar(usuario: Usuario): void {
    this.usuarioEditandoId.set(usuario.id);
    this.formulario.patchValue({
      name: usuario.name,
      username: usuario.username,
      email: usuario.email,
      phone: usuario.phone,
      website: usuario.website,
    });
    this.error.set('');
    this.mensaje.set(`Editando al usuario ${usuario.name}.`);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected cancelarEdicion(): void {
    this.usuarioEditandoId.set(null);
    this.formulario.reset({
      name: '',
      username: '',
      email: '',
      phone: '',
      website: '',
    });
  }

  protected eliminar(usuario: Usuario): void {
    const confirmado = window.confirm(`¿Desea eliminar al usuario ${usuario.name}?`);
    if (!confirmado) {
      return;
    }
    this.error.set('');
    this.mensaje.set('');
    this.usuarioService.eliminar(usuario.id).subscribe({
      next: () => {
        this.usuarios.update((actuales) => actuales.filter((item) => item.id !== usuario.id));
        this.mensaje.set('Usuario eliminado de la lista local.');
        this.exitosas.update((total) => total + 1);
      },
      error: (error) => {
        console.error('Error al eliminar:', error);
        this.error.set('No se pudo eliminar el usuario.');
      },
    });
  }

  protected cambiarCorreo(usuario: Usuario): void {
    const email = 'nuevo@correo.com';
    this.usuarioService.actualizarParcial(usuario.id, { email }).subscribe({
      next: (respuesta) => {
        this.usuarios.update((actuales) =>
          actuales.map((item) => (item.id === usuario.id ? { ...item, ...respuesta, email } : item)),
        );
        this.mensaje.set(`PATCH aplicado al usuario ${usuario.name}.`);
        this.exitosas.update((total) => total + 1);
      },
      error: (error) => {
        console.error('Error en PATCH:', error);
        this.error.set('No se pudo actualizar el correo.');
      },
    });
  }
}
