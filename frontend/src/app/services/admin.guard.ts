import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const adminGuard: CanActivateFn = () => inject(AuthService).user()?.role === 'ADMINISTRADOR' ? true : inject(Router).createUrlTree(['/error', '403']);
