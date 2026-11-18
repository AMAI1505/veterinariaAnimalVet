import { Component, OnDestroy, OnInit, signal } from '@angular/core';

@Component({
  standalone: true,
  styleUrl: './about.css',
  template: `
    <main class="about-page">
      <section class="hero" id="nosotros">
        <div class="hero-copy">
          <p class="eyebrow">CUIDADO VETERINARIO CON CARIÑO</p>
          <h1>Su bienestar,<br>nuestra prioridad.</h1>
          <p class="hero-description">En AnimalVet acompañamos a cada mascota con atención cercana, respeto y compromiso con su bienestar.</p>
        </div>
        <div class="hero-image"><img [src]="animalPhotos[currentPhoto()].src" [alt]="animalPhotos[currentPhoto()].alt" /><div class="photo-dots">@for(photo of animalPhotos;track photo.src;let i=$index){<button type="button" [class.active]="currentPhoto()===i" [attr.aria-label]="'Mostrar imagen '+(i+1)+' de animales'" [attr.aria-current]="currentPhoto()===i?'true':null" (click)="currentPhoto.set(i)"></button>}</div></div>
      </section>

      <section class="why-section" id="por-que-animalvet">
        <div class="section-intro"><p class="eyebrow">NUESTRO COMPROMISO</p><h2>¿Por qué elegir AnimalVet?</h2><p>Cuidamos de cada mascota como parte de la familia, en cada etapa de su vida.</p></div>
        <div class="reason-grid">
          <article class="reason-card"><span class="reason-icon" aria-hidden="true">♡</span><h3>Atención cercana</h3><p>Escuchamos a cada familia y brindamos una atención amable, pensada para las necesidades de cada mascota.</p></article>
          <article class="reason-card"><span class="reason-icon" aria-hidden="true">✚</span><h3>Cuidado integral</h3><p>Acompañamos la prevención, el cuidado diario y la salud de tus compañeros de cuatro patas.</p></article>
          <article class="reason-card"><span class="reason-icon" aria-hidden="true">◷</span><h3>Disponibles 24/7</h3><p>Estamos disponibles las 24 horas, todos los días, para acompañarte cuando tu mascota lo necesita.</p></article>
        </div>
      </section>

      <section class="find-section" id="ubicacion">
        <div class="find-copy"><p class="eyebrow">VISÍTANOS</p><h2>Encuéntranos en...</h2><p>Te esperamos en nuestra ubicación de referencia en Lima.</p><div class="location-note"><span aria-hidden="true">⌖</span><div><strong>Ubicación referencial</strong><p>Av. Bienestar Animal 123, Miraflores, Lima, Perú</p><small>Dirección ficticia para demostración.</small></div></div></div>
        <div class="map-frame"><iframe title="Mapa con la ubicación referencial de AnimalVet en Lima" src="https://maps.google.com/maps?q=-12.1234%2C-77.0300&z=15&output=embed" loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe><span class="map-caption">Ubicación ficticia de referencia</span></div>
      </section>
    </main>

    <footer class="about-footer" id="contacto">
      <div class="footer-main"><div class="footer-brand"><svg class="paw" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><ellipse cx="12" cy="16.8" rx="5.8" ry="4.2"/><ellipse cx="5.2" cy="10.6" rx="2" ry="2.7"/><ellipse cx="9.3" cy="6.6" rx="2" ry="2.6"/><ellipse cx="14.7" cy="6.6" rx="2" ry="2.6"/><ellipse cx="18.8" cy="10.6" rx="2" ry="2.7"/></svg><strong>Animal<span>Vet</span></strong><p>Cuidado y bienestar para quienes más quieres.</p></div>
        <div class="footer-contact"><h2>Contacto</h2><p>Atención veterinaria disponible 24 horas, los 7 días de la semana.</p><a class="about-button phone-link" href="tel:+51900000000"><span aria-hidden="true">☎</span> Llamar al +51 900 000 000</a></div>
        <div class="footer-location"><h2>Visítanos</h2><p>Av. Bienestar Animal 123<br>Miraflores, Lima, Perú</p></div>
      </div>
      <div class="footer-bottom"><span>© {{year}} AnimalVet</span><a href="#nosotros">Sobre nosotros</a></div>
    </footer>
  `
})
export class AboutComponent implements OnInit, OnDestroy {
  readonly currentPhoto = signal(0);
  readonly year = new Date().getFullYear();
  readonly animalPhotos = [
    { src: 'https://images.unsplash.com/photo-1739391262319-f3659f52bdfa?auto=format&fit=crop&w=1200&q=80', alt: 'Perro de pelaje café y blanco' },
    { src: 'https://images.unsplash.com/photo-1681237597031-c293cc488639?auto=format&fit=crop&w=1200&q=80', alt: 'Gato atigrado de ojos amarillos' },
    { src: 'https://images.unsplash.com/photo-1606391276068-d82696ac76bc?auto=format&fit=crop&w=1200&q=80', alt: 'Cachorro labrador de pelaje dorado' },
  ];
  private photoTimer?: ReturnType<typeof setInterval>;
  ngOnInit() { this.photoTimer = setInterval(() => this.currentPhoto.update(index => (index + 1) % this.animalPhotos.length), 5000); }
  ngOnDestroy() { if (this.photoTimer) clearInterval(this.photoTimer); }
}
