import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ResumenAdmin } from '../models/modelos';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  resumen(): Observable<ResumenAdmin> {
    return this.http.get<ResumenAdmin>(`${environment.apiUrl}/admin/resumen`);
  }
}
