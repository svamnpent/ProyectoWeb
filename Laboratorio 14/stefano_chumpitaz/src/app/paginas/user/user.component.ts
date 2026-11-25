import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { User, UserPayload } from '../../models/user';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-user',
  imports: [ReactiveFormsModule],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss'
})
export class UserComponent {
  private readonly userService = inject(UserService);
  private readonly fb = inject(FormBuilder);

  protected readonly users = signal<User[]>([]);
  protected readonly detalle = signal<User | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly mensaje = signal('');
  protected readonly editandoId = signal<number | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    username: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['']
  });

  constructor() {
    this.cargarUsuarios();
  }

  // GET /users
  protected cargarUsuarios(): void {
    this.cargando.set(true);
    this.error.set('');
    this.userService.getAllUsers().subscribe({
      next: usuarios => {
        this.users.set(usuarios);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de usuarios.');
        this.cargando.set(false);
      }
    });
  }

  // GET /users/{id}
  protected verDetalle(id: number): void {
    this.error.set('');
    this.userService.getUserById(id).subscribe({
      next: usuario => this.detalle.set(usuario),
      error: () => this.error.set(`No se pudo obtener el usuario ${id}.`)
    });
  }

  protected cerrarDetalle(): void {
    this.detalle.set(null);
  }

  protected editar(usuario: User): void {
    this.editandoId.set(usuario.id);
    this.form.reset({
      name: usuario.name,
      username: usuario.username,
      email: usuario.email,
      phone: usuario.phone
    });
    this.mensaje.set('');
  }

  protected cancelar(): void {
    this.editandoId.set(null);
    this.form.reset();
  }

  // POST /users  o  PUT /users/{id}
  protected guardar(): void {
    this.mensaje.set('');
    this.error.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const datos: UserPayload = this.form.getRawValue();
    const id = this.editandoId();

    if (id === null) {
      this.userService.createUser(datos).subscribe({
        next: creado => {
          this.users.update(lista => [...lista, { ...datos, ...creado }]);
          this.mensaje.set(`Usuario "${creado.name ?? datos.name}" creado.`);
          this.form.reset();
        },
        error: () => this.error.set('No se pudo crear el usuario.')
      });
    } else {
      this.userService.updateUser(id, datos).subscribe({
        next: actualizado => {
          this.users.update(lista =>
            lista.map(u => (u.id === id ? { ...u, ...datos, ...actualizado, id } : u))
          );
          this.mensaje.set(`Usuario ${id} actualizado.`);
          this.cancelar();
        },
        error: () => this.error.set(`No se pudo actualizar el usuario ${id}.`)
      });
    }
  }

  // DELETE /users/{id}
  protected eliminar(usuario: User): void {
    this.mensaje.set('');
    this.error.set('');
    this.userService.deleteUser(usuario.id).subscribe({
      next: () => {
        this.users.update(lista => lista.filter(u => u.id !== usuario.id));
        this.mensaje.set(`Usuario "${usuario.name}" eliminado.`);
        if (this.detalle()?.id === usuario.id) {
          this.detalle.set(null);
        }
      },
      error: () => this.error.set(`No se pudo eliminar el usuario ${usuario.id}.`)
    });
  }
}
