import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, firstValueFrom, map, of, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, RegisterResponse } from '../models/user.model';
import { environment } from '../../environments/environment';
import { SKIP_AUTH_REDIRECT } from '../interceptors/auth-context.token';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private userApiUrl = `${environment.apiUrl}/users`;
  private readonly tokenStorageKey = 'token';
  private readonly userNameStorageKey = 'userName';
  private readonly authenticatedSubject = new BehaviorSubject<boolean>(false);
  private unauthorizedRedirectInProgress = false;

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password } as LoginRequest).pipe(
      tap(res => {
        this.setSession(res.token, res.name);
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

  forgotPassword(email: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.apiUrl}/reset-password`, { token, newPassword });
  }

  initializeSession(): Promise<void> {
    if (!this.hasStoredToken()) {
      this.clearSession();
      return Promise.resolve();
    }

    return firstValueFrom(this.validateStoredSession().pipe(map(() => undefined)));
  }

  validateStoredSession(): Observable<boolean> {
    if (!this.hasStoredToken()) {
      this.clearSession();
      return of(false);
    }

    return this.http.get(`${this.userApiUrl}/me`, {
      context: new HttpContext().set(SKIP_AUTH_REDIRECT, true)
    }).pipe(
      map(() => true),
      tap((isValid) => {
        if (isValid) {
          this.authenticatedSubject.next(true);
          return;
        }

        this.clearSession();
      }),
      catchError(() => {
        this.clearSession();
        return of(false);
      })
    );
  }

  logout(): void {
    this.clearSession();
    this.unauthorizedRedirectInProgress = false;
    void this.router.navigate(['/login']);
  }

  handleUnauthorized(): void {
    this.clearSession();

    if (this.unauthorizedRedirectInProgress) {
      return;
    }

    this.unauthorizedRedirectInProgress = true;

    if (this.router.url === '/login') {
      this.unauthorizedRedirectInProgress = false;
      return;
    }

    void this.router.navigate(['/login']).finally(() => {
      this.unauthorizedRedirectInProgress = false;
    });
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenStorageKey);
  }

  isAuthenticated(): boolean {
    return this.authenticatedSubject.value;
  }

  getUserName(): string {
    return localStorage.getItem(this.userNameStorageKey) || '';
  }

  private hasStoredToken(): boolean {
    return !!this.getToken();
  }

  private setSession(token: string, userName: string): void {
    localStorage.setItem(this.tokenStorageKey, token);
    localStorage.setItem(this.userNameStorageKey, userName);
    this.authenticatedSubject.next(true);
  }

  private clearSession(): void {
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem(this.userNameStorageKey);
    this.authenticatedSubject.next(false);
  }
}