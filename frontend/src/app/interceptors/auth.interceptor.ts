import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';
import { SKIP_AUTH_REDIRECT } from './auth-context.token';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();

    if (token) {
      req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (this.shouldHandleUnauthorized(req, error)) {
          this.authService.handleUnauthorized();
        }

        return throwError(() => error);
      })
    );
  }

  private shouldHandleUnauthorized(req: HttpRequest<any>, error: HttpErrorResponse): boolean {
    if (req.context.get(SKIP_AUTH_REDIRECT)) {
      return false;
    }

    if (error.status !== 401 && error.status !== 403) {
      return false;
    }

    if (!req.url.startsWith(environment.apiUrl)) {
      return false;
    }

    return !req.url.includes('/auth/');
  }
}
