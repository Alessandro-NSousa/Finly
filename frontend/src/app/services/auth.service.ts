import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, RegisterResponse } from '../models/user.model';
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

  register(name: string, email: string, password: string, monthlyIncome: number): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, { name, email, password, monthlyIncome } as RegisterRequest);
  }

  activateAccount(token: string): Observable<{message: string}> {
    return this.http.get<{message: string}>(`${this.apiUrl}/activate`, { params: { token } });
  }

  resendVerificationEmail(email: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.apiUrl}/resend-verification`, { email });
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