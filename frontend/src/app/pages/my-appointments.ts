import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiService, Appointment } from '../services/api';

@Component({
  standalone: true,
  imports: [DatePipe],
  styleUrls: ['./simple-list.css','./my-appointments.css'],
  template: `
    <main class="page-shell customer-page">
      <div class="page-heading customer-page-heading"><h1>Citas de mis mascotas</h1></div>
      @if(error()){<div class="notice error" role="alert">{{error()}}</div>}
      <section class="panel">
        <div class="table-wrap"><table class="customer-appointments-table"><colgroup><col class="date-col"><col class="pet-col"><col class="professional-col"><col class="reason-col"><col class="status-col"></colgroup><thead><tr><th>Fecha y hora</th><th>Mascota</th><th>Profesional</th><th>Motivo</th><th>Estado</th></tr></thead><tbody>
          @for(cita of appointments();track cita.id){<tr><td>{{cita.fechaHora|date:'dd/MM/yyyy HH:mm'}}</td><td><strong>{{cita.mascota}}</strong></td><td>{{cita.veterinario}}</td><td>{{cita.motivo}}</td><td><span class="status" [class.completed]="cita.estado.toUpperCase()==='FINALIZADA'" [class.scheduled]="cita.estado.toUpperCase()==='PROGRAMADA'">{{cita.estado.toUpperCase()==='FINALIZADA'?'FINALIZADA':cita.estado}}</span></td></tr>}
          @empty{<tr><td colspan="5" class="empty-row">Aún no hay citas asociadas a tu correo.</td></tr>}
        </tbody></table></div>
      </section>
    </main>
  `
})
export class MyAppointmentsComponent implements OnInit {
  private readonly api=inject(ApiService);
  readonly appointments=signal<Appointment[]>([]);
  readonly error=signal('');
  private readonly today=this.localToday();
  ngOnInit(){void this.load();}
  async load(){try{const appointments=await this.api.myAppointments();this.appointments.set([...appointments].sort((a,b)=>{const statusOrder=(status:string)=>status.trim().toLocaleUpperCase()==='PROGRAMADA'?0:status.trim().toLocaleUpperCase()==='FINALIZADA'?1:2;const statusDifference=statusOrder(a.estado)-statusOrder(b.estado);if(statusDifference)return statusDifference;const aToday=a.fechaHora.slice(0,10)===this.today;const bToday=b.fechaHora.slice(0,10)===this.today;if(aToday!==bToday)return aToday?-1:1;return a.fechaHora.localeCompare(b.fechaHora);}));}catch{this.error.set('No se pudieron cargar tus citas. Comprueba tu sesión e inténtalo de nuevo.');}}
  private localToday(){const date=new Date();return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;}
}
