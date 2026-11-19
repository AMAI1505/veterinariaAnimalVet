import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, Appointment, Pet, Vet } from '../services/api';
import { AuthService } from '../services/auth';
import { HttpErrorResponse } from '@angular/common/http';

const REASONS_BY_SPECIALTY = [
  { specialty: 'Medicina General', reasons: ['Consulta general', 'Vacunación', 'Desparasitación'] },
  { specialty: 'Cirugía', reasons: ['Procedimiento quirúrgico'] },
  { specialty: 'Estética y peluquería', reasons: ['Grooming'] },
];

@Component({
  standalone: true,
  imports: [FormsModule, DatePipe, RouterLink],
  template: `
    <main class="page-shell appointments-page">
      <div class="page-heading appointment-page-heading"><div class="today-count"><span class="today-count-number">{{ todayAppointmentCount() }}</span><span class="today-count-label">{{ todayAppointmentCount() === 1 ? 'CITA' : 'CITAS' }} HOY</span></div>@if (canSchedule()) { <button class="button button-primary" (click)="openForm()">＋ Nueva cita</button> }</div>
      @if (error()) { <div class="notice error" role="alert">{{ error() }}</div> }
      <section class="panel">
        <div class="panel-heading appointments-toolbar">
          <div class="appointments-controls">
            <div class="view-switch" aria-label="Vista de citas">
              <button type="button" [class.active]="view() === 'table'" (click)="setView('table')">☷ Tabla</button>
              <button type="button" [class.active]="view() === 'calendar'" (click)="setView('calendar')">▦ Calendario</button>
            </div>
            @if (view() === 'calendar') {
              @if (calendarMode() === 'day') { <button type="button" class="calendar-back calendar-mode-switch" (click)="calendarMode.set('month')">Vista mensual</button> }
              @else { <h3 class="calendar-month-label">{{ calendarTitle() }}</h3> }
            }
            <div class="date-tools">
              <input class="date-filter" type="date" [(ngModel)]="selectedDate" (ngModelChange)="onDateSelected($event)" aria-label="Filtrar citas por fecha">
              @if (view() === 'calendar') { <div class="date-navigation" aria-label="Navegación de fechas"><button type="button" class="calendar-arrow" (click)="moveCalendar(-1)" aria-label="Anterior">‹</button><button type="button" class="calendar-arrow" (click)="moveCalendar(1)" aria-label="Siguiente">›</button><button type="button" class="today-button" (click)="goToToday()">Hoy</button></div> }
            </div>
          </div>
        </div>
        @if (view() === 'table') {
          <div class="table-wrap"><table class="appointments-table" [class.without-actions]="!hasAppointmentActions()"><thead><tr><th>Fecha y hora</th><th>Mascota</th><th>Tutor</th><th>Veterinario</th><th>Motivo</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>
            @for (cita of filteredAppointments(); track cita.id) { <tr><td>{{ cita.fechaHora | date:'dd/MM/yyyy HH:mm' }}</td><td><strong>{{ cita.mascota }}</strong></td><td>{{ cita.tutor }}</td><td>{{ cita.veterinario }}</td><td>{{ cita.motivo }}</td><td><span class="status" [class.completed]="cita.estado.toUpperCase()==='FINALIZADA'">{{ cita.estado }}</span></td><td class="row-actions">@if (isVet() && cita.estado.toUpperCase() !== 'FINALIZADA' && cita.estado.toUpperCase() !== 'CANCELADA') { <a class="button button-primary attend-action" [routerLink]="['/atencion',cita.id]"><svg class="attend-check" viewBox="0 0 20 20" aria-hidden="true"><path d="m3.5 10 4.2 4.2L16.5 5.5"/></svg>Atender</a> } @if (isGroomi() && cita.estado.toUpperCase() !== 'FINALIZADA' && cita.estado.toUpperCase() !== 'CANCELADA') { <button class="button button-primary attend-action" type="button" (click)="finalizeGrooming(cita)"><svg class="attend-check" viewBox="0 0 20 20" aria-hidden="true"><path d="m3.5 10 4.2 4.2L16.5 5.5"/></svg>Atender</button> } @if (canEditData() && cita.estado.toUpperCase() !== 'FINALIZADA') { <button class="icon-action edit-icon" type="button" (click)="openEdit(cita)" aria-label="Editar cita" title="Editar"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14.2 6.2 3.6 3.6M4 20l4.1-.9L19 8.2a2.55 2.55 0 0 0-3.6-3.6L4.5 15.5 4 20Z"/></svg></button><button class="icon-action delete-icon" type="button" (click)="remove(cita)" aria-label="Eliminar cita" title="Eliminar"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6m4-6v6M6 7l1 13h10l1-13M9 7V4h6v3"/></svg></button> }</td></tr> }
            @empty { <tr><td [attr.colspan]="hasAppointmentActions() ? 7 : 6" class="empty-row">{{ appointments().length ? (selectedDate ? 'No hay citas para la fecha seleccionada.' : 'No hay citas registradas.') : 'Aún no hay citas registradas.' }}</td></tr> }
          </tbody></table></div>
        } @else {
          @if (calendarMode() === 'month') {
            <div class="calendar-grid month-grid">
              @for (dayName of weekDays; track dayName) { <div class="weekday">{{ dayName }}</div> }
              @for (day of monthDays(); track day.key) {
                <button type="button" class="month-day" [class.outside-month]="!day.inMonth" [class.today]="day.key === today" (click)="selectCalendarDay(day.key)">
                  <span class="day-number">{{ day.number }}</span>
                  @if (appointmentsOn(day.key).length) { <span class="day-events">@for (cita of appointmentsOn(day.key).slice(0, 2); track cita.id) { <span class="calendar-event"><b>{{ appointmentTime(cita) }}</b> {{ cita.mascota }} · {{ cita.motivo }}</span> } @if (appointmentsOn(day.key).length > 2) { <span class="more-events">+{{ appointmentsOn(day.key).length - 2 }} más</span> }</span> }
                </button>
              }
            </div>
          } @else {
            <div class="day-agenda">
              @for (cita of appointmentsOn(selectedDay()); track cita.id) {
                <article class="agenda-appointment"><time>{{ appointmentTime(cita) }}</time><div class="agenda-details"><strong>{{ cita.mascota }}</strong><span>{{ cita.motivo }} · {{ cita.veterinario }}</span><small>Tutor: {{ cita.tutor }} · {{ cita.estado }}</small></div><div class="agenda-actions">@if (isVet() && cita.estado.toUpperCase() !== 'FINALIZADA' && cita.estado.toUpperCase() !== 'CANCELADA') { <a class="button button-primary attend-action" [routerLink]="['/atencion',cita.id]"><svg class="attend-check" viewBox="0 0 20 20" aria-hidden="true"><path d="m3.5 10 4.2 4.2L16.5 5.5"/></svg>Atender</a> } @if (isGroomi() && cita.estado.toUpperCase() !== 'FINALIZADA' && cita.estado.toUpperCase() !== 'CANCELADA') { <button class="button button-primary attend-action" type="button" (click)="finalizeGrooming(cita)"><svg class="attend-check" viewBox="0 0 20 20" aria-hidden="true"><path d="m3.5 10 4.2 4.2L16.5 5.5"/></svg>Atender</button> } @if (canEditData() && cita.estado.toUpperCase() !== 'FINALIZADA') { <button class="icon-action edit-icon" type="button" (click)="openEdit(cita)" aria-label="Editar cita" title="Editar"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14.2 6.2 3.6 3.6M4 20l4.1-.9L19 8.2a2.55 2.55 0 0 0-3.6-3.6L4.5 15.5 4 20Z"/></svg></button> }</div></article>
              } @empty { <p class="agenda-empty">{{ appointments().length ? 'No hay citas para la fecha seleccionada.' : 'No hay citas registradas para este día.' }}</p> }
            </div>
          }
        }
      </section>
    </main>
    @if (formOpen()) {
      <div class="modal-backdrop" (click)="closeForm()"><section class="modal-card" role="dialog" aria-modal="true" aria-labelledby="form-title" (click)="$event.stopPropagation()">
        <div class="modal-header"><div><p class="eyebrow">CITAS CLÍNICAS</p><h2 id="form-title">{{appointmentId?'Editar cita':'Nueva cita'}}</h2></div><button type="button" class="icon-button" (click)="closeForm()" aria-label="Cerrar">×</button></div>
        @if (formError()) { <div class="notice error">{{ formError() }}</div> }
        <form (ngSubmit)="save()">
          <div class="pet-picker"><label for="pet-search">Mascota</label><input id="pet-search" name="petSearch" [ngModel]="petSearch" (ngModelChange)="searchPets($event)" placeholder="Busca por nombre de mascota o de su dueño" autocomplete="off">@if(petSearch.trim() && !petId){<div class="pet-results" role="listbox">@for(pet of filteredPets();track pet.id){<button type="button" class="pet-result" role="option" (click)="selectPet(pet)"><strong>{{pet.nombre}}</strong><span>Dueño: {{pet.tutor}}</span></button>}@empty{<p class="pet-no-results">No se encontraron mascotas con ese nombre de dueño o mascota.</p>}</div>}</div>
          <label>Motivo de la consulta<select name="reason" [(ngModel)]="motivo" (ngModelChange)="changeMotivo($event)" required><option value="">Elige un motivo</option>@for(group of reasonGroups;track group.specialty){@for(reason of group.reasons;track reason){<option [value]="reason">{{reason}}</option>}}@if(motivo&&!isKnownReason(motivo)){<option [value]="motivo">Motivo registrado: {{motivo}}</option>}</select></label>
          <label>Profesional<select name="vet" [(ngModel)]="vetId" (ngModelChange)="onProfessionalChange()" [disabled]="!specialty" required><option [ngValue]="null">{{specialty?'Elige profesional':'Elige primero el motivo'}}</option>@for (vet of vetsForSpecialty(); track vet.id) { <option [ngValue]="vet.id">{{ vet.nombre }}</option> }</select></label>
          <div class="form-row"><label>Fecha<input name="date" type="date" [(ngModel)]="date" [min]="today" (ngModelChange)="loadAvailability()" required></label>
            <label>Hora disponible<select name="time" [(ngModel)]="time" (ngModelChange)="canSave.set(!!time)" [disabled]="!slots().length" required><option value="">{{slots().length?'Elige un horario':availability()?'Sin horarios disponibles':'Selecciona profesional y fecha'}}</option>@for (slot of slots(); track slot) { <option [value]="slot">{{ slot }}</option> }</select></label></div>
          @if(availabilityMessage()){<p class="muted helper">{{availabilityMessage()}}</p>}
          <p class="muted helper">Los horarios duran 30 minutos, respetan el turno del profesional y deben iniciar al menos 30 minutos después de la hora actual.</p>
          <div class="modal-actions"><button type="button" class="button button-quiet" (click)="closeForm()">Cancelar</button><button class="button button-primary" type="submit" [disabled]="saving() || !canSave()">{{ saving() ? 'Guardando…' : (appointmentId?'Guardar cambios':'Guardar cita') }}</button></div>
        </form>
      </section></div>
    }
    @if (groomingConfirmation(); as cita) {
      <div class="modal-backdrop confirmation-backdrop" (click)="closeGroomingConfirmation()"><section class="modal-card grooming-confirmation" role="dialog" aria-modal="true" aria-labelledby="grooming-confirm-title" (click)="$event.stopPropagation()">
        <button class="icon-button confirmation-close" type="button" aria-label="Cerrar" (click)="closeGroomingConfirmation()" [disabled]="confirmingGrooming()">×</button>
        <div class="confirmation-mark" aria-hidden="true">✓</div><p class="eyebrow">GROOMING · ANIMALVET</p><h2 id="grooming-confirm-title">¿Finalizar esta cita?</h2>
        <p class="confirmation-copy">Confirma que la atención de grooming ya terminó. El estado de la cita cambiará a <strong>Finalizada</strong>.</p>
        <div class="confirmation-appointment"><strong>{{cita.mascota}}</strong><span>{{cita.fechaHora|date:'dd/MM/yyyy · HH:mm'}} · {{cita.motivo}}</span></div>
        @if(confirmationError()){<div class="notice error" role="alert">{{confirmationError()}}</div>}
        <div class="modal-actions confirmation-actions"><button type="button" class="button button-quiet" (click)="closeGroomingConfirmation()" [disabled]="confirmingGrooming()">Cancelar</button><button type="button" class="button button-primary" (click)="confirmGroomingFinalization()" [disabled]="confirmingGrooming()">{{confirmingGrooming()?'Guardando…':'Sí, finalizar cita'}}</button></div>
      </section></div>
    }
  `,
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  readonly reasonGroups = REASONS_BY_SPECIALTY;
  readonly appointments = signal<Appointment[]>([]); readonly pets = signal<Pet[]>([]); readonly vets = signal<Vet[]>([]);
  readonly availability = signal<{ turno: string; disponibles: string[] } | null>(null); readonly slots = signal<string[]>([]);
  readonly availabilityMessage=signal('');
  readonly formOpen = signal(false); readonly error = signal(''); readonly formError = signal('');
  readonly saving = signal(false); readonly canSave = signal(false);
  readonly groomingConfirmation = signal<Appointment|null>(null); readonly confirmingGrooming = signal(false); readonly confirmationError = signal('');
  readonly view = signal<'table'|'calendar'>('table'); readonly calendarMode = signal<'month'|'day'>('month');
  readonly selectedDay = signal(this.localToday()); readonly calendarMonth = signal(this.monthStart(this.localToday()));
  readonly weekDays = ['Dom','Lun','Mar','Mié','Jue','Vie','Sáb'];
  selectedDate = '';
  petId: number | null = null; petSearch=''; specialty=''; vetId: number | null = null; date = ''; time = ''; motivo = ''; appointmentId:number|null=null;
  readonly today = this.localToday();
  canSchedule() { const role=this.auth.user()?.role; return role==='ADMINISTRADOR'||role==='RECEPCION'; }
  canEditData() { return this.auth.user()?.role==='ADMINISTRADOR'; }
  isVet() { const user=this.auth.user(); return user?.role==='PERSONAL_VETERINARIA' && user.tipoPersonal==='VETERINARIO'; }
  isGroomi() { const user=this.auth.user(); return user?.role==='PERSONAL_VETERINARIA' && user.tipoPersonal==='ESTILISTA'; }
  hasAppointmentActions() { if(this.canEditData())return this.filteredAppointments().length>0;if(!this.isVet()&&!this.isGroomi())return false;return this.filteredAppointments().some(cita=>!['FINALIZADA','CANCELADA'].includes(cita.estado.trim().toLocaleUpperCase())); }
  filteredAppointments() { return this.appointments().filter(cita => !this.selectedDate || this.appointmentDay(cita) === this.selectedDate); }
  todayAppointmentCount() { return this.appointments().filter(cita=>this.appointmentDay(cita)===this.today&&cita.estado.trim().toLocaleUpperCase()==='PROGRAMADA').length; }
  appointmentsOn(day:string) { return this.filteredAppointments().filter(cita=>this.appointmentDay(cita)===day).sort((a,b)=>a.fechaHora.localeCompare(b.fechaHora)); }
  appointmentTime(cita:Appointment) { return cita.fechaHora.slice(11,16); }
  monthDays() {
    const [year,month]=this.calendarMonth().split('-').map(Number); const first=new Date(year,month-1,1); const gridStart=new Date(year,month-1,1-first.getDay());
    return Array.from({length:42},(_,i)=>{const d=new Date(gridStart.getFullYear(),gridStart.getMonth(),gridStart.getDate()+i);const key=this.dateKey(d);return {key,number:d.getDate(),inMonth:d.getMonth()===month-1};});
  }
  calendarTitle() { const [year,month]=this.calendarMonth().split('-').map(Number);const label=new Date(year,month-1,1).toLocaleDateString('es-PE',{month:'long'});return label.charAt(0).toLocaleUpperCase('es-PE')+label.slice(1); }
  selectCalendarDay(day:string) { this.selectedDay.set(day);this.calendarMode.set('day'); }
  moveCalendar(direction:number) { const anchor=this.calendarMode()==='day'?this.selectedDay():`${this.calendarMonth()}-01`;const [year,month,day]=anchor.split('-').map(Number);const moved=this.calendarMode()==='day'?new Date(year,month-1,day+direction):new Date(year,month-1+direction,1);const key=this.dateKey(moved);this.selectedDate=key;this.selectedDay.set(key);this.calendarMonth.set(this.monthStart(key)); }
  goToToday() { this.selectedDate=this.today;this.selectedDay.set(this.today);this.calendarMonth.set(this.monthStart(this.today));if(this.view()==='calendar')this.calendarMode.set('day'); }
  onDateSelected(day:string) { if(!day)return;this.selectedDay.set(day);this.calendarMonth.set(this.monthStart(day));if(this.view()==='calendar')this.calendarMode.set('day'); }
  setView(view:'table'|'calendar') { this.view.set(view);if(view==='calendar'&&this.selectedDate){this.selectedDay.set(this.selectedDate);this.calendarMonth.set(this.monthStart(this.selectedDate));this.calendarMode.set('day');} }
  ngOnInit() { void this.load(); }
  async load() {
    try {
      const appointments = await this.api.appointments();
      this.appointments.set([...appointments].sort((a, b) => {
        const statusOrder = (status: string) => status.trim().toLocaleUpperCase() === 'PROGRAMADA' ? 0 : status.trim().toLocaleUpperCase() === 'FINALIZADA' ? 1 : 2;
        const statusDifference = statusOrder(a.estado) - statusOrder(b.estado);
        if (statusDifference) return statusDifference;
        const aIsToday = this.appointmentDay(a) === this.today;
        const bIsToday = this.appointmentDay(b) === this.today;
        if (aIsToday !== bIsToday) return aIsToday ? -1 : 1;
        return a.fechaHora.localeCompare(b.fechaHora);
      }));
      if (this.canSchedule()) {
        const [pets, vets] = await Promise.all([this.api.pets(), this.api.vets()]);
        this.pets.set(pets); this.vets.set(vets);
      }
      this.error.set('');
    }
    catch { this.error.set('No se pudieron cargar las citas. Comprueba tu sesión e inténtalo de nuevo.'); }
  }
  async openForm() {
    this.formOpen.set(true); this.formError.set(''); this.availabilityMessage.set(''); this.date = this.today; this.time = ''; this.petId = null; this.petSearch=''; this.specialty=''; this.vetId = null; this.motivo = ''; this.appointmentId=null; this.canSave.set(false);
    if (!this.pets().length || !this.vets().length) { try { const [pets, vets] = await Promise.all([this.api.pets(), this.api.vets()]); this.pets.set(pets); this.vets.set(vets); } catch { this.formError.set('No se pudieron cargar mascotas o profesionales.'); } }
  }
  async openEdit(cita:Appointment){if(!this.canEditData())return;this.formOpen.set(true);this.formError.set('');this.appointmentId=cita.id;this.date=cita.fechaHora.slice(0,10);this.time='';this.petId=cita.mascotaId;this.petSearch='';this.vetId=cita.veterinarioId;this.specialty='';this.motivo=this.canonicalReason(cita.motivo)??cita.motivo;this.canSave.set(false);try{const [pets,vets]=await Promise.all([this.api.pets(),this.api.vets()]);this.pets.set(pets);this.vets.set(vets);this.petSearch=this.selectedPetLabel();const assignedVet=vets.find(v=>v.id===cita.veterinarioId);this.specialty=this.reasonSpecialty(this.motivo)??assignedVet?.especialidad??'';if(this.reasonSpecialty(this.motivo)&&assignedVet?.especialidad!==this.specialty)this.vetId=null;await this.loadAvailability();this.time=cita.fechaHora.slice(11,16);this.canSave.set(this.vetId===cita.veterinarioId&&this.slots().includes(this.time));}catch{this.formError.set('No se pudo consultar la disponibilidad para editar.');}}
  searchPets(value:string){this.petSearch=value;const selected=this.pets().find(p=>p.id===this.petId);if(selected&&value!==`${selected.nombre} · ${selected.tutor}`)this.petId=null;}
  filteredPets(){const term=this.petSearch.trim().toLocaleLowerCase();return this.pets().filter(p=>`${p.nombre} ${p.tutor}`.toLocaleLowerCase().includes(term));}
  selectPet(pet:Pet){this.petId=pet.id;this.petSearch=`${pet.nombre} · ${pet.tutor}`;}
  selectedPetLabel(){const pet=this.pets().find(p=>p.id===this.petId);return pet?`${pet.nombre} · ${pet.tutor}`:'';}
  vetsForSpecialty(){return this.vets().filter(v=>v.especialidad===this.specialty);}
  changeMotivo(value:string){this.motivo=value;this.specialty=this.reasonSpecialty(value)??'';this.vetId=null;void this.loadAvailability();}
  async onProfessionalChange(){
    await this.loadAvailability();
    if(!this.vetId||this.date!==this.today||this.slots().length||!this.availability())return;
    for(let offset=1;offset<=14;offset++){
      const nextDate=this.dateAfterDays(this.today,offset);
      try{const next=await this.api.availability(this.vetId,nextDate);if(next.disponibles.length){this.date=nextDate;this.availability.set(next);this.slots.set(next.disponibles);this.availabilityMessage.set('No quedaban horarios para hoy. Seleccioné la siguiente fecha con disponibilidad.');return;}}
      catch{this.availabilityMessage.set('No se pudo consultar la disponibilidad. Intenta elegir otra fecha.');return;}
    }
    this.availabilityMessage.set('No hay horarios disponibles en los próximos 14 días para este profesional.');
  }
  isKnownReason(value:string){return this.reasonSpecialty(value)!==null;}
  private reasonSpecialty(value:string){return REASONS_BY_SPECIALTY.find(group=>group.reasons.some(reason=>reason.toLocaleLowerCase()===value.trim().toLocaleLowerCase()))?.specialty??null;}
  private canonicalReason(value:string){return REASONS_BY_SPECIALTY.flatMap(group=>group.reasons).find(reason=>reason.toLocaleLowerCase()===value.trim().toLocaleLowerCase())??null;}
  closeForm() { this.formOpen.set(false); }
  async loadAvailability() {
    this.time = ''; this.slots.set([]); this.availability.set(null); this.availabilityMessage.set(''); this.canSave.set(false);
    if (this.vetId == null || !this.date || this.date < this.today) return;
    try { const data = await this.api.availability(this.vetId, this.date, this.appointmentId ?? undefined); this.availability.set(data); this.slots.set(data.disponibles); }
    catch { this.formError.set('No se pudo consultar la disponibilidad.'); }
  }
  async save() {
    if (this.appointmentId && !this.canEditData()) return;
    if (!this.canSave() || !this.vetId || !this.petId || !this.time) return;
    this.saving.set(true); this.formError.set('');
    try { const dto={fechaHora:`${this.date}T${this.time}:00`,mascotaId:this.petId,veterinarioId:this.vetId,motivo:this.motivo}; if(this.appointmentId) await this.api.updateAppointment(this.appointmentId,dto); else await this.api.createAppointment(dto); this.closeForm(); await this.load(); }
    catch (e) { const response=e instanceof HttpErrorResponse ? e.error as {message?:string;detail?:string} : null; this.formError.set(response?.message ?? response?.detail ?? 'Ese horario acaba de ocuparse. Actualiza y elige otra hora.'); await this.loadAvailability(); }
    finally { this.saving.set(false); }
  }
  async finalizeGrooming(cita:Appointment){
    if(!this.isGroomi()||cita.estado.toUpperCase()==='FINALIZADA'||cita.estado.toUpperCase()==='CANCELADA')return;
    this.confirmationError.set('');this.groomingConfirmation.set(cita);
  }
  closeGroomingConfirmation(){if(!this.confirmingGrooming())this.groomingConfirmation.set(null);}
  async confirmGroomingFinalization(){
    const cita=this.groomingConfirmation();if(!cita||!this.isGroomi()||this.confirmingGrooming())return;
    this.confirmingGrooming.set(true);this.confirmationError.set('');
    try{await this.api.finalizeGroomingAppointment(cita.id);this.groomingConfirmation.set(null);await this.load();}
    catch{this.confirmationError.set('No se pudo finalizar la cita. Comprueba tu sesión e inténtalo de nuevo.');}
    finally{this.confirmingGrooming.set(false);}
  }
  async remove(cita:Appointment){if(!this.canEditData()||!confirm(`¿Eliminar la cita de ${cita.mascota}?`))return;try{await this.api.deleteAppointment(cita.id);await this.load();}catch{this.error.set('No se pudo eliminar la cita.');}}
  private localToday() { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
  private appointmentDay(cita:Appointment) { return cita.fechaHora.slice(0,10); }
  private monthStart(day:string) { return `${day.slice(0,7)}-01`; }
  private dateKey(d:Date) { return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
  private dateAfterDays(date:string,days:number){const [year,month,day]=date.split('-').map(Number);const next=new Date(year,month-1,day+days);return `${next.getFullYear()}-${String(next.getMonth()+1).padStart(2,'0')}-${String(next.getDate()).padStart(2,'0')}`;}
}
