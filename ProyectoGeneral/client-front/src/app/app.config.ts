import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { AuthService } from './core/services/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // withComponentInputBinding: los parámetros de ruta (/producto/:id) llegan como input() al componente.
    provideRouter(routes, withComponentInputBinding()),
    // El orden importa: primero se agrega el token, luego se manejan los errores de la respuesta.
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    // Antes de mostrar la primera pantalla, recupera la sesión guardada (si existe).
    provideAppInitializer(() => firstValueFrom(inject(AuthService).restaurarSesion())),
  ],
};
