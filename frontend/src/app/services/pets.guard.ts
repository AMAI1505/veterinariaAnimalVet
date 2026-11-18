import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const petsGuard: CanActivateFn = async () => {
  const auth = inject(AuthService);
  await auth.loadSession();
  const user = auth.user();
  const allowed = user?.role === 'ADMINISTRADOR' || user?.role === 'RECEPCION'
    || (user?.role === 'PERSONAL_VETERINARIA' && user.tipoPersonal === 'VETERINARIO');
  return allowed ? true : inject(Router).createUrlTree(['/error', '403']);
};
