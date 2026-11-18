import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login';
import { DashboardComponent } from './pages/dashboard';
import { authGuard } from './services/auth.guard';
import { PetsComponent } from './pages/pets';
import { DoctorsComponent } from './pages/doctors';
import { MedicalVisitComponent } from './pages/medical-visit';
import { adminGuard } from './services/admin.guard';
import { veterinarianGuard } from './services/veterinarian.guard';
import { staffGuard } from './services/staff.guard';
import { MyScheduleComponent } from './pages/my-schedule';
import { AboutComponent } from './pages/about';
import { RegisterComponent } from './pages/register';
import { MyAppointmentsComponent } from './pages/my-appointments';
import { customerGuard } from './services/customer.guard';
import { internalGuard } from './services/internal.guard';
import { petsGuard } from './services/pets.guard';
import { ArticlesComponent } from './pages/articles';
import { ErrorPageComponent } from './pages/error-page';

export const routes: Routes = [
  { path: 'error/:status', component: ErrorPageComponent },
  // Compatibility with old bookmarks from when Spring served Angular under /app.
  { path: 'app', redirectTo: '', pathMatch: 'prefix' },
  { path: '', component: AboutComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegisterComponent },
  { path: 'sobre-nosotros', component: AboutComponent },
  { path: 'articulos', component: ArticlesComponent, pathMatch: 'full' },
  { path: 'articulos/:slug', component: ArticlesComponent },
  { path: 'mis-citas', component: MyAppointmentsComponent, canActivate: [authGuard, customerGuard] },
  { path: 'citas', component: DashboardComponent, canActivate: [authGuard, internalGuard] },
  { path: 'mascotas', component: PetsComponent, canActivate: [authGuard, petsGuard] },
  { path: 'admin/medicos', component: DoctorsComponent, canActivate: [authGuard, adminGuard] },
  { path: 'atencion/:citaId', component: MedicalVisitComponent, canActivate: [authGuard, veterinarianGuard] },
  { path: 'mi-horario', component: MyScheduleComponent, canActivate: [authGuard, staffGuard] },
  { path: '**', component: ErrorPageComponent, data: { status: '404' } }
];
