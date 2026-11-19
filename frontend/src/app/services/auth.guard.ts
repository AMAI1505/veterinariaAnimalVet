import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const authGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  await auth.loadSession();
  return auth.user() ? true : inject(Router).parseUrl('/login');
};
