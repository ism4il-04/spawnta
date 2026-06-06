import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);

  constructor(private http: HttpClient) {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      this.currentUserSubject.next(JSON.parse(savedUser));
    }
  }

  signup(data: any): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/signup`, data);
    // No setSession — user must verify email before logging in
  }

  login(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, data).pipe(
      tap(res => this.setSession(res))
    );
  }

  verifyEmail(token: string): Observable<AuthResponse> {
    return this.http.get<AuthResponse>(`${this.apiUrl}/verify-email`, { params: { token } }).pipe(
      tap(res => this.setSession(res))
    );
  }

  resendVerification(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/resend-verification`, { email });
  }

  refresh(): Observable<AuthResponse> {
    const user = this.currentUserValue;
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken: user?.refreshToken }).pipe(
      tap(res => this.setSession(res))
    );
  }

  logout() {
    const user = this.currentUserValue;
    if (user?.refreshToken) {
      this.http.post(`${this.apiUrl}/logout`, { refreshToken: user.refreshToken }).subscribe();
    }
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  private setSession(authRes: AuthResponse) {
    localStorage.setItem('currentUser', JSON.stringify(authRes));
    this.currentUserSubject.next(authRes);
  }

  public get currentUser$(): Observable<AuthResponse | null> {
    return this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  public isLoggedIn(): boolean {
    return !!this.currentUserValue;
  }

  public getAccessToken(): string | null {
    return this.currentUserValue?.accessToken ?? null;
  }

  public updateCurrentUserName(firstName: string, lastName: string): void {
    const user = this.currentUserValue;
    if (!user) return;
    this.setSession({ ...user, firstName, lastName });
  }

  public hasRefreshToken(): boolean {
    return !!this.currentUserValue?.refreshToken;
  }
}

