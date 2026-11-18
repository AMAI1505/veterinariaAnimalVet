import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login';
import { AboutComponent } from './pages/about';
import { RegisterComponent } from './pages/register';
import { MyAppointmentsComponent } from './pages/my-appointments';
import { customerGuard } from './services/customer.guard';
import { authGuard } from './services/auth.guard';
import { ArticlesComponent } from './pages/articles';
import { ErrorPageComponent } from './pages/error-page';
import { PetsComponent } from './pages/pets';
import { petsGuard } from './services/pets.guard';

export const routes: Routes = [
  { path: 'error/:status', component: ErrorPageComponent },
  { path: '', component: AboutComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegisterComponent },
  { path: 'sobre-nosotros', component: AboutComponent },
  { path: 'articulos', component: ArticlesComponent, pathMatch: 'full' },
  { path: 'articulos/:slug', component: ArticlesComponent },
  {
    path: 'mis-citas',
    component: MyAppointmentsComponent,
    canActivate: [authGuard, customerGuard]
  },
  {
    path: 'mis-mascotas',
    component: PetsComponent,
    canActivate: [authGuard, petsGuard]
  },
  { path: '**', component: ErrorPageComponent, data: { status: '404' } }
];