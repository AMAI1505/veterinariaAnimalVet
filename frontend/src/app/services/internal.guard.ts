import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const internalGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  await auth.loadSession();
  const role = auth.user()?.role;
  return ['ADMINISTRADOR', 'RECEPCION', 'PERSONAL_VETERINARIA'].includes(role ?? '')
    ? true : router.createUrlTree(['/error', '403']);
};
