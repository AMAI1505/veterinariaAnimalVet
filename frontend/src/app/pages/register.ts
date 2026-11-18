import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../services/auth';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  styleUrl: './login-layout.css',
  template: `
    <main class="login-page register-page">
      <section class="login-layout register-layout">
        <aside class="login-visual"><img class="visual-art" src="/assets/login-pets.svg" alt="Ilustración de un perro y un gato" /></aside>
        <section class="login-card">
          <p class="eyebrow">CUENTA DE TUTOR</p>
          @if (registered()) {
            <h1>Cuenta creada</h1><p class="muted">{{successMessage()}}</p>
            <a class="button button-primary button-wide register-login-button" routerLink="/login">Ir a iniciar sesión</a>
          } @else {
            <h1>Crear cuenta</h1><p class="muted">Usa el mismo correo y DNI registrados en la ficha de tu mascota.</p>
            <form (ngSubmit)="submit()">
              <div class="register-fields">
                <label for="register-email">Correo electrónico<input id="register-email" name="email" type="email" [(ngModel)]="email" autocomplete="email" maxlength="50" required></label>
                <label for="register-dni">DNI del dueño<input id="register-dni" name="dni" [(ngModel)]="dni" inputmode="numeric" autocomplete="off" pattern="[0-9]{8}" maxlength="8" required></label>
                <label for="register-password">Contraseña<input id="register-password" name="password" type="password" [(ngModel)]="password" autocomplete="new-password" minlength="10" maxlength="100" pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s]).+" title="Incluye mayúscula, minúscula, número y símbolo" required></label>
                <label for="confirm-password">Confirmar contraseña<input id="confirm-password" name="confirmPassword" type="password" [(ngModel)]="confirmPassword" autocomplete="new-password" minlength="10" maxlength="100" required></label>
              </div>
              @if (error()) { <p class="form-error" role="alert">{{error()}}</p> }
              <button class="button button-primary button-wide" type="submit" [disabled]="loading()">{{loading()?'Creando cuenta…':'Crear cuenta'}}</button>
            </form>
            <p class="register-link">¿Ya tienes cuenta? <a routerLink="/login">Inicia sesión</a></p>
          }
        </section>
      </section>
    </main>
  `
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  email=''; dni=''; password=''; confirmPassword='';
  readonly loading=signal(false); readonly error=signal(''); readonly registered=signal(false); readonly successMessage=signal('');

  async submit() {
    this.error.set('');
    const email=this.email.trim().toLowerCase();
    if(this.password!==this.confirmPassword){this.error.set('Las contraseñas no coinciden.');return;}
    this.loading.set(true);
    try {
      const result=await this.auth.register(email,this.dni.trim(),this.password);
      this.successMessage.set(result.mensaje);this.registered.set(true);
    } catch (error) {
      const response=error instanceof HttpErrorResponse?error.error as {message?:string;detail?:string}:null;
      this.error.set(response?.message??response?.detail??'No se pudo crear la cuenta. Verifica que el correo y DNI coincidan con la ficha de tu mascota.');
    } finally { this.loading.set(false); }
  }
}
