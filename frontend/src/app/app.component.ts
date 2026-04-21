import { Component } from '@angular/core';
import { AuthService } from './services/auth.service';
import { ThemeService } from './services/theme.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Finly';

  constructor(
    public authService: AuthService,
    private themeService: ThemeService
  ) {}

  logout(): void {
    this.authService.logout();
  }

  getUserName(): string {
    return this.authService.getUserName() || 'Usuário';
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  isDarkTheme(): boolean {
    return this.themeService.isDarkTheme();
  }

  getThemeLabel(): string {
    return this.isDarkTheme() ? 'Escuro' : 'Claro';
  }

  getThemeActionLabel(): string {
    return this.isDarkTheme() ? 'Ativar modo claro' : 'Ativar modo escuro';
  }
}
