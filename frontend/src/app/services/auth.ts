import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface SessionUser { username: string; name: string; role: string; tipoPersonal: string | null; }
interface LoginResponse extends SessionUser { token: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<SessionUser | null>(null);
  private readonly http = inject(HttpClient);

  async loadSession(): Promise<void> {
    if (typeof sessionStorage === 'undefined' || !sessionStorage.getItem('animalvet_access_token')) {
      this.user.set(null);
      return;
    }
    try { this.user.set(await firstValueFrom(this.http.get<SessionUser>('/api/auth/me'))); }
    catch { sessionStorage.removeItem('animalvet_access_token'); this.user.set(null); }
  }

  async login(username: string, password: string): Promise<SessionUser> {
    const response = await firstValueFrom(this.http.post<LoginResponse>('/api/auth/login', { username, password }));
    sessionStorage.setItem('animalvet_access_token', response.token);
    const { token: _token, ...user } = response;
    this.user.set(user);
    return user;
  }

  async register(correo: string, dni: string, password: string) {
    return firstValueFrom(this.http.post<{correo:string;mensaje:string}>('/api/auth/register', { correo, dni, password }));
  }

  async logout(): Promise<void> {
    try { await firstValueFrom(this.http.post('/api/auth/logout', {})); }
    finally { sessionStorage.removeItem('animalvet_access_token'); this.user.set(null); }
  }
}
