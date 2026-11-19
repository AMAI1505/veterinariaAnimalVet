import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth';

@Component({
  selector: 'av-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    @if (isLoginRoute()) {
      <header class="topbar">
        <a class="brand" routerLink="/sobre-nosotros"><svg class="brand-paw" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><ellipse cx="12" cy="16.8" rx="5.8" ry="4.2"/><ellipse cx="5.2" cy="10.6" rx="2" ry="2.7"/><ellipse cx="9.3" cy="6.6" rx="2" ry="2.6"/><ellipse cx="14.7" cy="6.6" rx="2" ry="2.6"/><ellipse cx="18.8" cy="10.6" rx="2" ry="2.7"/></svg><span class="brand-name">Animal<span>Vet</span></span></a>
        <button class="menu-toggle" type="button" (click)="toggleMenu()" [attr.aria-expanded]="menuOpen()" aria-controls="main-navigation" [attr.aria-label]="menuOpen() ? 'Cerrar menú' : 'Abrir menú'"><span></span><span></span><span></span></button>
        <nav id="main-navigation" [class.open]="menuOpen()" (click)="closeMenu()"><a routerLink="/sobre-nosotros" [class.active]="isAboutRoute()">Sobre nosotros</a><a routerLink="/articulos" routerLinkActive="active">Artículos</a></nav>
        <div class="account" [class.menu-open]="menuOpen()"><a class="button button-quiet" routerLink="/registro" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="closeMenu()">Registrarse</a><a class="button button-quiet" routerLink="/login" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="closeMenu()">Iniciar sesión</a></div>
      </header>
    } @else if (auth.user(); as user) {
      <header class="topbar">
        <a class="brand" [routerLink]="user.role === 'CLIENTE' ? '/mis-citas' : '/citas'"><svg class="brand-paw" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><ellipse cx="12" cy="16.8" rx="5.8" ry="4.2"/><ellipse cx="5.2" cy="10.6" rx="2" ry="2.7"/><ellipse cx="9.3" cy="6.6" rx="2" ry="2.6"/><ellipse cx="14.7" cy="6.6" rx="2" ry="2.6"/><ellipse cx="18.8" cy="10.6" rx="2" ry="2.7"/></svg><span class="brand-name">Animal<span>Vet</span></span></a>
        <button class="menu-toggle" type="button" (click)="toggleMenu()" [attr.aria-expanded]="menuOpen()" aria-controls="main-navigation" [attr.aria-label]="menuOpen() ? 'Cerrar menú' : 'Abrir menú'"><span></span><span></span><span></span></button>
        <nav id="main-navigation" [class.open]="menuOpen()" (click)="closeMenu()"><a routerLink="/sobre-nosotros" [class.active]="isAboutRoute()">Sobre nosotros</a><a routerLink="/articulos" routerLinkActive="active">Artículos</a>
          @if (user.role === 'CLIENTE') { <a routerLink="/mis-citas" routerLinkActive="active">Mis citas</a> }
          @else { <a routerLink="/citas" routerLinkActive="active">Citas</a>@if (user.role !== 'PERSONAL_VETERINARIA' || user.tipoPersonal === 'VETERINARIO') { <a routerLink="/mascotas" routerLinkActive="active">Mascotas</a> }
            @if (user.role === 'ADMINISTRADOR') { <a routerLink="/admin/medicos" routerLinkActive="active">Personal</a> }
            @if (user.role === 'PERSONAL_VETERINARIA') { <a routerLink="/mi-horario" routerLinkActive="active">Mi horario</a> }
          }
        </nav>
        <div class="account" [class.menu-open]="menuOpen()"><span class="account-name"><svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c.5-3.8 3.1-6 7-6s6.5 2.2 7 6"/></svg>{{ user.name }}</span><button class="button button-quiet" (click)="closeMenu(); logout()">Cerrar sesión</button></div>
      </header>
    } @else {
      <header class="topbar">
        <a class="brand" routerLink="/sobre-nosotros"><svg class="brand-paw" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><ellipse cx="12" cy="16.8" rx="5.8" ry="4.2"/><ellipse cx="5.2" cy="10.6" rx="2" ry="2.7"/><ellipse cx="9.3" cy="6.6" rx="2" ry="2.6"/><ellipse cx="14.7" cy="6.6" rx="2" ry="2.6"/><ellipse cx="18.8" cy="10.6" rx="2" ry="2.7"/></svg><span class="brand-name">Animal<span>Vet</span></span></a>
        <button class="menu-toggle" type="button" (click)="toggleMenu()" [attr.aria-expanded]="menuOpen()" aria-controls="main-navigation" [attr.aria-label]="menuOpen() ? 'Cerrar menú' : 'Abrir menú'"><span></span><span></span><span></span></button>
        <nav id="main-navigation" [class.open]="menuOpen()" (click)="closeMenu()"><a routerLink="/sobre-nosotros" [class.active]="isAboutRoute()">Sobre nosotros</a><a routerLink="/articulos" routerLinkActive="active">Artículos</a></nav>
        <div class="account" [class.menu-open]="menuOpen()"><a class="button button-quiet" routerLink="/registro" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="closeMenu()">Registrarse</a><a class="button button-quiet" routerLink="/login" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="closeMenu()">Iniciar sesión</a></div>
      </header>
    }
    <router-outlet />
  `,
  styleUrl: './app.css'
})
export class AppComponent {
  readonly auth = inject(AuthService);
  readonly menuOpen = signal(false);
  private readonly router = inject(Router);
  constructor() { this.auth.loadSession(); }
  isLoginRoute() { return this.router.url.startsWith('/login'); }
  isAboutRoute() { return this.router.url === '/' || this.router.url.startsWith('/sobre-nosotros'); }
  toggleMenu() { this.menuOpen.update(open => !open); }
  closeMenu() { this.menuOpen.set(false); }
  async logout() { await this.auth.logout(); await this.router.navigateByUrl('/login'); }
}
