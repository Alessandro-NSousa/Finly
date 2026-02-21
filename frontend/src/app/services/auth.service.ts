import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password } as LoginRequest).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('userName', res.name);
      })
    );
  }

  register(name: string, email: string, password: string, monthlyIncome: number): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, { name, email, password, monthlyIncome } as RegisterRequest).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('userName', res.name);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getUserName(): string {
    return localStorage.getItem('userName') || '';
  }
}
