import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const token = typeof sessionStorage === 'undefined' ? null : sessionStorage.getItem('animalvet_access_token');
  const authorizedRequest = token && request.url.startsWith('/api/')
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorizedRequest).pipe(catchError((error: unknown) => {
    if (error instanceof HttpErrorResponse) {
      const isLoginRequest = request.url.includes('/api/auth/login') || request.url.includes('/api/auth/register');
      if (error.status === 401 && !isLoginRequest) {
        if (typeof sessionStorage !== 'undefined') sessionStorage.removeItem('animalvet_access_token');
        void router.navigate(['/error', '401']);
      } else if (error.status === 0 || error.status === 403 || error.status === 404 || error.status >= 500) {
        const status = error.status === 0 || error.status === 503 ? '503'
          : error.status >= 500 ? '500' : String(error.status);
        void router.navigate(['/error', status]);
      }
    }
    return throwError(() => error);
  }));
};
