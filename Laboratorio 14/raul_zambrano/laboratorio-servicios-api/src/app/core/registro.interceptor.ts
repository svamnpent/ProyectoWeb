import { HttpInterceptorFn } from '@angular/common/http';

/** Registra método y URL. No escribe cuerpos ni cabeceras. */
export const registroInterceptor: HttpInterceptorFn = (req, next) => {
  console.info('HTTP', req.method, req.url.split('?')[0]);
  return next(req);
};
