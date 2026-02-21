import { Injectable } from '@angular/core';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private auth0: Auth0Service) {}

  /** Redirect to Auth0 Universal Login */
  login(): void {
    this.auth0.loginWithRedirect();
  }

  /** Redirect to Auth0 Universal Login with signup screen */
  loginWithSignup(): void {
    this.auth0.loginWithRedirect({
      authorizationParams: { screen_hint: 'signup' }
    });
  }

  /** Log out and redirect back to /login */
  logout(): void {
    this.auth0.logout({ logoutParams: { returnTo: window.location.origin + '/login' } });
  }

  get isAuthenticated$(): Observable<boolean> {
    return this.auth0.isAuthenticated$;
  }

  get user$() {
    return this.auth0.user$;
  }
}
