import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <main class="login-page">
      <section class="login-layout">
        <aside class="login-visual">
          <img class="visual-art" src="/assets/login-pets.svg" alt="Ilustración de un perro y un gato" />
        </aside>
        <section class="login-card">
          <p class="eyebrow">SISTEMA DE GESTIÓN VETERINARIA</p>
          <h1>Bienvenido a AnimalVet</h1><p class="muted">Inicia sesión para continuar</p>
          <form (ngSubmit)="submit()">
            <label for="username">Correo o usuario</label><input id="username" name="username" [(ngModel)]="username" autocomplete="username" required>
            <label for="password">Contraseña</label><input id="password" name="password" type="password" [(ngModel)]="password" autocomplete="current-password" required>
            @if (error()) { <p class="form-error" role="alert">{{ error() }}</p> }
            <button class="button button-primary button-wide" type="submit" [disabled]="loading()">{{ loading() ? 'Ingresando…' : 'Iniciar sesión' }}</button>
          </form>
          <p class="register-link">¿Tu mascota está registrada? <a routerLink="/registro">Crea tu cuenta</a></p>
        </section>
      </section>
    </main>
  `,
  styleUrl: './login-layout.css'
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  username = '';
  password = '';
  readonly error = signal('');
  readonly loading = signal(false);

  async submit() {
    this.loading.set(true); this.error.set('');
    try {
      const user = await this.auth.login(this.username, this.password);
      await this.router.navigateByUrl(user.role === 'CLIENTE' ? '/mis-citas' : user.role === 'ADMINISTRADOR' ? '/admin/medicos' : '/citas');
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        const message = error.error && typeof error.error === 'object' && typeof error.error.message === 'string'
          ? error.error.message : null;
        this.error.set(message
          ? `La API respondió 401: ${message}`
          : 'El servidor respondió 401 sin un mensaje de la API.');
      } else if (error instanceof HttpErrorResponse && error.status === 0) {
        this.error.set('No se pudo conectar con la API. Revisa que el túnel siga activo.');
      } else if (error instanceof HttpErrorResponse) {
        this.error.set(`La solicitud de inicio de sesión falló (HTTP ${error.status}).`);
      } else {
        this.error.set('No se pudo completar el inicio de sesión. Inténtalo de nuevo.');
      }
    }
    finally { this.loading.set(false); }
  }
}
