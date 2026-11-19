import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, Shift } from '../services/api';

const WEEK = [
  { key: 'LUNES', label: 'Lunes' },
  { key: 'MARTES', label: 'Martes' },
  { key: 'MIERCOLES', label: 'Miércoles' },
  { key: 'JUEVES', label: 'Jueves' },
  { key: 'VIERNES', label: 'Viernes' },
  { key: 'SABADO', label: 'Sábado' },
  { key: 'DOMINGO', label: 'Domingo' },
];

@Component({ standalone:true, imports:[FormsModule], styleUrl:'./my-schedule.css', template:`
<main class="page-shell">
  <div class="schedule-date-control"><input aria-label="Seleccionar fecha" id="schedule-date" name="schedule-date" type="date" [(ngModel)]="selectedDate"><button type="button" class="date-nav-button" aria-label="Semana anterior" (click)="moveWeek(-1)">‹</button><button type="button" class="date-nav-button" aria-label="Semana siguiente" (click)="moveWeek(1)">›</button><button type="button" class="date-nav-button today-button" (click)="selectedDate=today()">Hoy</button></div>
  <section class="panel schedule-panel">
    @if (shifts().length) {
      <div class="schedule-scroll">
        <div class="week-calendar">
          <div class="week-header"><div class="time-corner"></div>@for(day of week;track day.key){<div class="day-header"><strong>{{day.label}}</strong><span>{{dateLabel(day.key)}}</span></div>}</div>
          <div class="week-body" [style.height.px]="hourCount() * hourHeight">
            <div class="time-axis">@for(hour of hours();track hour){<div class="hour-label" [style.height.px]="hourHeight">{{hourLabel(hour)}}</div>}</div>
            @for(day of week;track day.key){
              <div class="day-column">
                @if(shiftFor(day.key);as shift){
                  @if(hasBreak(shift)){
                    <div class="shift-block" [style.top.px]="shiftTop(shift)" [style.height.px]="beforeBreakHeight(shift)"><strong>{{day.label}}</strong><span>{{formatTime(shift.horaInicio)}} – {{formatTime(shift.horaDescansoInicio)}}</span><small>Turno laboral</small></div>
                    <div class="break-gap" [style.top.px]="breakColumnTop(shift)" [style.height.px]="breakColumnHeight(shift)"></div>
                    <div class="shift-block second-shift-block" [style.top.px]="afterBreakTop(shift)" [style.height.px]="afterBreakHeight(shift)"><span>{{breakEnd(shift)}} – {{formatTime(shift.horaFin)}}</span><small>Turno laboral</small></div>
                  } @else {<div class="shift-block" [style.top.px]="shiftTop(shift)" [style.height.px]="shiftHeight(shift)"><strong>{{day.label}}</strong><span>{{formatTime(shift.horaInicio)}} – {{formatTime(shift.horaFin)}}</span><small>Turno laboral</small></div>}
                }
                @else {<span class="day-off">Descanso</span>}
              </div>
            }
          </div>
        </div>
      </div>
    } @else { <div class="schedule-empty"><span>◷</span><h3>No tienes turnos configurados</h3><p>Cuando administración te asigne un horario, aparecerá aquí.</p></div> }
  </section>
</main>` })
export class MyScheduleComponent implements OnInit {
  private readonly api=inject(ApiService);
  readonly shifts=signal<Shift[]>([]);
  readonly week=WEEK;
  readonly hourHeight=64;
  selectedDate=this.today();
  async ngOnInit(){try{this.shifts.set(await this.api.mySchedule());}catch{this.shifts.set([]);}}
  shiftFor(day:string){return this.shifts().find(shift=>shift.diaSemana===day);}
  startHour(){const starts=this.shifts().map(shift=>this.minutes(shift.horaInicio)).filter((value):value is number=>value!==null);return Math.min(8,...starts.map(value=>Math.floor(value/60)));}
  endHour(){const ends=this.shifts().map(shift=>this.minutes(shift.horaFin)).filter((value):value is number=>value!==null);return Math.max(18,...ends.map(value=>Math.ceil(value/60)));}
  hourCount(){return this.endHour()-this.startHour();}
  hours(){return Array.from({length:this.hourCount()},(_,index)=>this.startHour()+index);}
  hourLabel(hour:number){return `${String(hour).padStart(2,'0')}:00`;}
  dateLabel(day:string){const [year,month,date]=this.selectedDate.split('-').map(Number);const selected=new Date(year,month-1,date);selected.setDate(selected.getDate()-((selected.getDay()+6)%7)+this.week.findIndex(item=>item.key===day));return selected.toLocaleDateString('es-PE',{day:'2-digit',month:'short'});}
  moveWeek(amount:number){const [year,month,date]=this.selectedDate.split('-').map(Number);const selected=new Date(year,month-1,date);selected.setDate(selected.getDate()+amount*7);this.selectedDate=`${selected.getFullYear()}-${String(selected.getMonth()+1).padStart(2,'0')}-${String(selected.getDate()).padStart(2,'0')}`;}
  formatTime(value:string|null){return value?.slice(0,5)??'--:--';}
  shiftTop(shift:Shift){const start=this.minutes(shift.horaInicio)??this.startHour()*60;return ((start-this.startHour()*60)/60)*this.hourHeight+5;}
  shiftHeight(shift:Shift){const start=this.minutes(shift.horaInicio),end=this.minutes(shift.horaFin);return Math.max(48,((end??start??0)-(start??0))/60*this.hourHeight-10);}
  hasBreak(shift:Shift){return !!shift.horaDescansoInicio&&!!shift.duracionDescansoMinutos;}
  beforeBreakHeight(shift:Shift){const start=this.minutes(shift.horaInicio)??0,pause=this.minutes(shift.horaDescansoInicio)??start;return Math.max(24,(pause-start)/60*this.hourHeight-10);}
  breakColumnTop(shift:Shift){const start=this.minutes(shift.horaInicio)??0,pause=this.minutes(shift.horaDescansoInicio)??start;return ((pause-this.startHour()*60)/60)*this.hourHeight+5;}
  breakColumnHeight(shift:Shift){return Math.max(12,(shift.duracionDescansoMinutos??0)/60*this.hourHeight-10);}
  afterBreakTop(shift:Shift){const endBreak=(this.minutes(shift.horaDescansoInicio)??0)+(shift.duracionDescansoMinutos??0);return ((endBreak-this.startHour()*60)/60)*this.hourHeight+5;}
  afterBreakHeight(shift:Shift){const endBreak=(this.minutes(shift.horaDescansoInicio)??0)+(shift.duracionDescansoMinutos??0),end=this.minutes(shift.horaFin)??endBreak;return Math.max(24,(end-endBreak)/60*this.hourHeight-10);}
  breakEnd(shift:Shift){const end=(this.minutes(shift.horaDescansoInicio)??0)+(shift.duracionDescansoMinutos??0);return `${String(Math.floor(end/60)).padStart(2,'0')}:${String(end%60).padStart(2,'0')}`;}
  private today(){const date=new Date();return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;}
  private minutes(value:string|null){if(!value)return null;const [hours,minutes]=value.slice(0,5).split(':').map(Number);return hours*60+minutes;}
}
