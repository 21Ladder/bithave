import { Injectable, signal } from '@angular/core';

export type Theme = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'bithave-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>('system');

  constructor() {
    this.apply(this.read());
  }

  apply(theme: Theme): void {
    this.theme.set(theme);
    const root = document.documentElement;

    if (theme === 'system') {
      root.removeAttribute('data-theme');
    } else {
      root.setAttribute('data-theme', theme);
    }

    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch {
      // private mode, the theme just does not persist
    }
  }

  toggle(): void {
    this.apply(this.resolved() === 'dark' ? 'light' : 'dark');
  }

  // What the user actually sees right now
  resolved(): 'light' | 'dark' {
    const current = this.theme();
    if (current !== 'system') return current;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  private read(): Theme {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === 'light' || stored === 'dark' || stored === 'system') return stored;
    } catch {
      // ignore storage errors
    }
    return 'system';
  }
}
