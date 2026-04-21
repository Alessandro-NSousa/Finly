import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly storageKey = 'theme';
  private readonly themeSubject = new BehaviorSubject<ThemeMode>('light');

  readonly theme$ = this.themeSubject.asObservable();

  private currentTheme: ThemeMode = 'light';
  private mediaQuery?: MediaQueryList;

  constructor(@Inject(DOCUMENT) private document: Document) {}

  initializeTheme(): void {
    if (typeof window === 'undefined') {
      return;
    }

    this.mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const storedTheme = this.getStoredTheme();
    const initialTheme = storedTheme ?? (this.mediaQuery.matches ? 'dark' : 'light');

    this.applyTheme(initialTheme);
    this.mediaQuery.addEventListener('change', (event) => {
      if (!this.getStoredTheme()) {
        this.applyTheme(event.matches ? 'dark' : 'light');
      }
    });
  }

  getCurrentTheme(): ThemeMode {
    return this.currentTheme;
  }

  isDarkTheme(): boolean {
    return this.currentTheme === 'dark';
  }

  toggleTheme(): void {
    this.setTheme(this.isDarkTheme() ? 'light' : 'dark');
  }

  setTheme(theme: ThemeMode): void {
    localStorage.setItem(this.storageKey, theme);
    this.applyTheme(theme);
  }

  private applyTheme(theme: ThemeMode): void {
    this.currentTheme = theme;
    this.document.documentElement.setAttribute('data-theme', theme);
    this.document.documentElement.style.colorScheme = theme;
    this.themeSubject.next(theme);
  }

  private getStoredTheme(): ThemeMode | null {
    const storedTheme = localStorage.getItem(this.storageKey);
    return storedTheme === 'dark' || storedTheme === 'light' ? storedTheme : null;
  }
}