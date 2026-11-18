import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface Pet { id: number; nombre: string; especie: string; sexo: 'HEMBRA' | 'MACHO' | null; esterilizado: boolean | null; raza: string; peso: string; color: string; tutor: string; dniTutor: string; telefonoTutor: string; correoTutor: string | null; }
export interface Vet { id: number; nombre: string; correo: string; especialidad: string; }
export interface Appointment { id: number; fechaHora: string; mascotaId: number; mascota: string; tutor: string; dniTutor: string; telefonoTutor: string; veterinarioId: number | null; veterinario: string; motivo: string; estado: string; }
export interface Availability { ocupados: string[]; disponibles: string[]; turno: string; }
export interface Shift { diaSemana: string; activo: boolean; horaInicio: string | null; horaFin: string | null; horaDescansoInicio: string | null; duracionDescansoMinutos: number | null; }
export interface Doctor { id: number; nombreCompleto: string; correo: string; telefono: string; especialidad: string; tipoPersonal: 'VETERINARIO' | 'ESTILISTA'; turnos: Shift[]; }
export interface ClinicalContext { cita: { id: number; fechaHora: string; motivo: string; estado: string; mascotaId: number; mascota: string; especie: string; sexo: 'HEMBRA' | 'MACHO' | null; esterilizado: boolean | null; color: string; raza: string; pesoRegistrado: string; tutorId: number; tutor: string; dniTutor: string; telefono: string }; atencion: { pesoKg: number|null; exploracionClinica: string|null; diagnostico: string; tratamiento: string; indicacionesTutor: string|null; proximoControl: string|null }|null; }
export interface MedicalHistory { id:number; citaFecha:string; fechaAtencion:string|null; diagnostico:string|null; tratamiento:string|null; exploracionClinica:string|null; pesoKg:number|null; indicacionesTutor:string|null; proximoControl:string|null; veterinario:string; motivo:string; estado:string; atencionRegistrada:boolean; }

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  appointments() { return firstValueFrom(this.http.get<Appointment[]>('/api/citas')); }
  myAppointments() { return firstValueFrom(this.http.get<Appointment[]>('/api/citas/mis-citas')); }
  pets() { return firstValueFrom(this.http.get<Pet[]>('/api/mascotas')); }
  vets() { return firstValueFrom(this.http.get<Vet[]>('/api/citas/veterinarios')); }
  availability(veterinarianId: number, date: string, excludeAppointmentId?: number) { const excluded=excludeAppointmentId?`&excluirCitaId=${excludeAppointmentId}`:''; return firstValueFrom(this.http.get<Availability>(`/api/citas/disponibilidad?veterinarioId=${veterinarianId}&fecha=${date}${excluded}`)); }
  createAppointment(data: { fechaHora: string; mascotaId: number; veterinarioId: number; motivo: string }) {
    return firstValueFrom(this.http.post<Appointment>('/api/citas', data));
  }
  updateAppointment(id:number,data:{fechaHora:string;mascotaId:number;veterinarioId:number;motivo:string}){return firstValueFrom(this.http.put<Appointment>(`/api/citas/${id}`,data));}
  deleteAppointment(id:number){return firstValueFrom(this.http.delete<void>(`/api/citas/${id}`));}
  finalizeGroomingAppointment(id:number){return firstValueFrom(this.http.post<Appointment>(`/api/citas/${id}/finalizar-grooming`,{}));}
  createPet(data: Omit<Pet, 'id'>) { return firstValueFrom(this.http.post<Pet>('/api/mascotas', data)); }
  updatePet(id: number, data: Omit<Pet, 'id'>) { return firstValueFrom(this.http.put<Pet>(`/api/mascotas/${id}`, data)); }
  deletePet(id: number) { return firstValueFrom(this.http.delete<void>(`/api/mascotas/${id}`)); }
  doctors() { return firstValueFrom(this.http.get<Doctor[]>('/api/admin/doctores')); }
  createDoctor(data: { nombreCompleto: string; correo: string; password: string; telefono: string; tipoPersonal: 'VETERINARIO'|'ESTILISTA'; especialidad: string; turnos: Shift[] }) { return firstValueFrom(this.http.post<Doctor>('/api/admin/doctores', data)); }
  updateDoctor(id:number,data:{nombreCompleto:string;correo:string;password:string;telefono:string;tipoPersonal:'VETERINARIO'|'ESTILISTA';especialidad:string;turnos:Shift[]}){return firstValueFrom(this.http.put<Doctor>(`/api/admin/doctores/${id}`,data));}
  updateShifts(id: number, shifts: Shift[]) { return firstValueFrom(this.http.put<Doctor>(`/api/admin/doctores/${id}/turnos`, shifts)); }
  deleteDoctor(id: number) { return firstValueFrom(this.http.delete<void>(`/api/admin/doctores/${id}`)); }
  registerClient(data: {correo:string;dni:string;password:string}) { return firstValueFrom(this.http.post<{correo:string;mensaje:string}>('/api/auth/register',data)); }
  clinicalContext(id: number) { return firstValueFrom(this.http.get<ClinicalContext>(`/api/atenciones/${id}`)); }
  medicalHistory(petId:number) { return firstValueFrom(this.http.get<MedicalHistory[]>(`/api/atenciones/mascota/${petId}/historial`)); }
  mySchedule() { return firstValueFrom(this.http.get<Shift[]>('/api/atenciones/mi-horario')); }
  saveClinicalRecord(id: number, data: { pesoKg: number|null; exploracionClinica: string; diagnostico: string; tratamiento: string; proximoControl: string|null }) { return firstValueFrom(this.http.post(`/api/atenciones/${id}`, data)); }
}
