import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

export const staffGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.user()?.role === 'PERSONAL_VETERINARIA' ? true : inject(Router).createUrlTree(['/error/403']);
};
