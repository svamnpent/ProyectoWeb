import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserPayload } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);

  // API pública de pruebas (REST). Recurso: /users
  private readonly apiUrl = 'https://jsonplaceholder.typicode.com/users';

  // GET /users -> lista de usuarios
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  // GET /users/{id} -> un usuario
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  // POST /users -> crea un usuario
  createUser(user: UserPayload): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  // PUT /users/{id} -> reemplaza los datos del usuario
  updateUser(id: number, user: UserPayload): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user);
  }

  // PATCH /users/{id} -> modifica solo algunos campos
  patchUser(id: number, cambios: Partial<UserPayload>): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/${id}`, cambios);
  }

  // DELETE /users/{id} -> elimina el usuario
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
