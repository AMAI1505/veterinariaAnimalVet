import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const customerGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  await auth.loadSession();
  return auth.user()?.role === 'CLIENTE' ? true : router.createUrlTree(['/error', '403']);
};
