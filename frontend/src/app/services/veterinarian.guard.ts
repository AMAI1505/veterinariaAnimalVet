import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

export const veterinarianGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.user()?.role === 'PERSONAL_VETERINARIA' && auth.user()?.tipoPersonal === 'VETERINARIO') return true;
  return router.createUrlTree(['/error/403']);
};
