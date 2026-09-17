import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, of, tap } from 'rxjs';
import { API_BASE_URL } from '../core/api.config';
import { Credentials, CurrentUser } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly base = inject(API_BASE_URL);

  private readonly currentUser = signal<CurrentUser | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isSignedIn = computed(() => this.currentUser() !== null);
  readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  // Called once at startup, an anonymous visitor simply has no user
  restore(): Observable<CurrentUser | null> {
    return this.http.get<CurrentUser>(`${this.base}/auth/me`).pipe(
      tap((user) => this.currentUser.set(user)),
      catchError(() => {
        this.currentUser.set(null);
        return of(null);
      })
    );
  }

  login(credentials: Credentials): Observable<CurrentUser> {
    return this.http
      .post<CurrentUser>(`${this.base}/auth/login`, credentials)
      .pipe(tap((user) => this.currentUser.set(user)));
  }

  register(credentials: Credentials): Observable<CurrentUser> {
    return this.http.post<CurrentUser>(`${this.base}/auth/register`, credentials);
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.base}/auth/logout`, {})
      .pipe(tap(() => this.currentUser.set(null)));
  }
}
