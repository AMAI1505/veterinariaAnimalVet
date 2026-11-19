import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { ApiService, ClinicalContext } from '../services/api';

@Component({standalone:true,imports:[FormsModule,RouterLink,DatePipe],styleUrl:'./medical-visit.css',template:`
<main class="page-shell"><a class="back-link" routerLink="/citas">← Volver a citas</a>
@if(error()){<div class="notice error" role="alert">{{error()}}</div>}
@if(context();as data){<header class="visit-heading"><div><h1>Atención médica</h1><p class="muted">{{data.cita.fechaHora | date:'dd/MM/yyyy HH:mm'}} · {{data.cita.motivo}}</p></div><span class="status">{{data.cita.estado}}</span></header>
<h2 class="patient-name">{{data.cita.mascota}}</h2><section class="patient-profile"><div class="paw-badge" aria-hidden="true">🐾</div><div class="patient-facts"><p><strong>Especie:</strong> {{data.cita.especie}}</p><p><strong>Raza:</strong> {{data.cita.raza}}</p><p><strong>Sexo:</strong> {{data.cita.sexo === 'HEMBRA' ? 'Hembra' : data.cita.sexo === 'MACHO' ? 'Macho' : 'No registrado'}}</p><p><strong>Dueño:</strong> {{data.cita.tutor}}</p><p><strong>Peso:</strong> {{data.cita.pesoRegistrado}} kg</p><p><strong>Esterilizado:</strong> {{data.cita.esterilizado === true ? 'Sí' : data.cita.esterilizado === false ? 'No' : 'No registrado'}}</p><p><strong>Color:</strong> {{data.cita.color}}</p></div></section>
@if(data.atencion){<section class="panel saved-record"><h2>Consulta registrada</h2><p><strong>Diagnóstico:</strong> {{data.atencion.diagnostico}}</p><p><strong>Tratamiento:</strong> {{data.atencion.tratamiento}}</p><p>{{data.atencion.exploracionClinica}}</p></section>}
@else{<form class="panel medical-form" (ngSubmit)="save()"><h2>Registro de consulta</h2><div class="form-row"><label>Peso actual (kg)<input name="weight" type="number" min="0.01" step="0.01" [(ngModel)]="pesoKg"></label><label>Próximo control<input name="next" type="date" [(ngModel)]="proximoControl" [min]="today"></label></div><label>Exploración clínica y hallazgos<textarea name="exam" rows="4" maxlength="4000" [(ngModel)]="exploracionClinica"></textarea></label><label>Diagnóstico <span class="required">*</span><textarea name="diagnosis" rows="3" maxlength="4000" [(ngModel)]="diagnostico" required></textarea></label><label>Receta médica <span class="required">*</span><textarea name="treatment" rows="4" maxlength="4000" placeholder="Fármacos, dosis, frecuencia y duración" [(ngModel)]="tratamiento" required></textarea></label><div class="form-actions"><a class="button button-quiet" routerLink="/citas">Cancelar</a><button class="button button-primary" [disabled]="saving()">{{saving()?'Guardando…':'Guardar y finalizar cita'}}</button></div></form>}
} @else if(loading()){<section class="panel loading">Cargando cita y datos del paciente…</section>}
</main>`})
export class MedicalVisitComponent implements OnInit {
  private readonly api=inject(ApiService);private readonly route=inject(ActivatedRoute);private readonly router=inject(Router);
  readonly context=signal<ClinicalContext|null>(null);readonly error=signal('');readonly loading=signal(true);readonly saving=signal(false);
  pesoKg:number|null=null;proximoControl='';exploracionClinica='';diagnostico='';tratamiento='';readonly today=this.localToday();
  async ngOnInit(){try{const id=Number(this.route.snapshot.paramMap.get('citaId'));this.context.set(await this.api.clinicalContext(id));}catch{this.error.set('No se pudo cargar la cita. Verifica que tengas acceso como veterinario.');}finally{this.loading.set(false);}}
  async save(){const id=this.context()?.cita.id;if(!id)return;this.saving.set(true);this.error.set('');try{await this.api.saveClinicalRecord(id,{pesoKg:this.pesoKg,exploracionClinica:this.exploracionClinica,diagnostico:this.diagnostico,tratamiento:this.tratamiento,proximoControl:this.proximoControl||null});await this.router.navigateByUrl('/citas');}catch{this.error.set('No se pudo guardar. Comprueba los campos obligatorios y que la cita siga disponible.');}finally{this.saving.set(false);}}
  private localToday(){const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;}
}
