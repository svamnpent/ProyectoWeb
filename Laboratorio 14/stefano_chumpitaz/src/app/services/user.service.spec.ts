import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { User, UserPayload } from '../models/user';

const API = 'https://jsonplaceholder.typicode.com/users';
const ana: User = { id: 1, name: 'Ana', username: 'ana', email: 'ana@mail.com', phone: '999' };
const payload: UserPayload = { name: 'Ana', username: 'ana', email: 'ana@mail.com', phone: '999' };

describe('UserService', () => {
  let service: UserService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getAllUsers hace GET /users', () => {
    let resultado: User[] = [];
    service.getAllUsers().subscribe(r => (resultado = r));
    const req = http.expectOne(API);
    expect(req.request.method).toBe('GET');
    req.flush([ana]);
    expect(resultado).toEqual([ana]);
  });

  it('getUserById hace GET /users/{id}', () => {
    service.getUserById(1).subscribe();
    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(ana);
  });

  it('createUser hace POST /users con el cuerpo', () => {
    service.createUser(payload).subscribe();
    const req = http.expectOne(API);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 11, ...payload });
  });

  it('updateUser hace PUT /users/{id}', () => {
    service.updateUser(1, payload).subscribe();
    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 1, ...payload });
  });

  it('patchUser hace PATCH /users/{id}', () => {
    service.patchUser(1, { phone: '111' }).subscribe();
    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ phone: '111' });
    req.flush({ ...ana, phone: '111' });
  });

  it('deleteUser hace DELETE /users/{id}', () => {
    service.deleteUser(1).subscribe();
    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
