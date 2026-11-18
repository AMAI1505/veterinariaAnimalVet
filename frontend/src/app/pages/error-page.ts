import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

type ErrorInfo = { title: string; message: string; detail: string; icon: string };

const ERROR_INFO: Record<string, ErrorInfo> = {
  '400': { title: 'Solicitud no válida', message: 'No pudimos procesar lo que enviaste.', detail: 'Revisa la información e inténtalo nuevamente.', icon: '!' },
  '401': { title: 'Inicia sesión', message: 'Tu sesión no está disponible o ya venció.', detail: 'Vuelve a iniciar sesión para continuar.', icon: '↗' },
  '403': { title: 'Acceso restringido', message: 'No tienes permiso para ver esta página.', detail: 'Si crees que deberías tener acceso, consulta con el administrador.', icon: '⌑' },
  '404': { title: 'No encontramos esta página', message: 'Puede que la dirección haya cambiado o que ya no exista.', detail: 'Revisa el enlace o vuelve a una sección disponible.', icon: '?' },
  '500': { title: 'Tuvimos un problema', message: 'El sistema encontró un error al completar tu solicitud.', detail: 'Inténtalo de nuevo en unos momentos.', icon: '!' },
  '503': { title: 'Servicio temporalmente no disponible', message: 'En este momento no podemos atender tu solicitud.', detail: 'Espera un momento y vuelve a intentarlo.', icon: '…' }
};

@Component({
  selector: 'av-error-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <main class="error-shell">
      <section class="error-card" aria-labelledby="error-title">
        <div class="error-art" aria-hidden="true">
          <div class="paw-mark"><span>🐾</span></div>
          <span class="error-code">{{ status }}</span>
        </div>
        <div class="error-copy">
          <p class="eyebrow">ANIMAL<span>VET</span> · {{ status }}</p>
          <h1 id="error-title">{{ info.title }}</h1>
          <p class="error-message">{{ info.message }}</p>
          <p class="error-detail">{{ info.detail }}</p>
          <div class="error-actions">
            <button class="button button-primary" type="button" (click)="goBack()">Volver</button>
            @if (status === '401') {
              <a class="button button-quiet" routerLink="/login">Iniciar sesión</a>
            } @else {
              <a class="button button-quiet" routerLink="/sobre-nosotros">Ir al inicio</a>
            }
          </div>
        </div>
      </section>
    </main>
  `,
  styleUrl: './error-page.css'
})
export class ErrorPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  get status(): string {
    const requested = this.route.snapshot.paramMap.get('status') ?? '404';
    return ERROR_INFO[requested] ? requested : '500';
  }

  get info(): ErrorInfo { return ERROR_INFO[this.status]; }

  goBack(): void {
    if (globalThis.history.length > 1) globalThis.history.back();
    else void this.router.navigateByUrl('/sobre-nosotros');
  }
}
