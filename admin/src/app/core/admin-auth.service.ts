import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';


export interface AdminAuthResponse {
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
export class AdminAuthService {
  private readonly apiUrl = `${environment.apiUrl}/api/auth`;

  private readonly currentAdminSubject = new BehaviorSubject<AdminAuthResponse | null>(null);

  constructor(private http: HttpClient) {
    const savedAdmin = localStorage.getItem('adminUser');
    if (savedAdmin) {
      this.currentAdminSubject.next(JSON.parse(savedAdmin));
    }
  }

  get currentAdmin$(): Observable<AdminAuthResponse | null> {
    return this.currentAdminSubject.asObservable();
  }

  login(email: string, password: string): Observable<AdminAuthResponse> {
    return this.http.post<AdminAuthResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(response => {
        if (response.role !== 'ADMIN') {
          throw new Error('Compte non autorise pour l administration.');
        }
        localStorage.setItem('adminUser', JSON.stringify(response));
        this.currentAdminSubject.next(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('adminUser');
    this.currentAdminSubject.next(null);
  }

  get currentAdminValue(): AdminAuthResponse | null {
    return this.currentAdminSubject.value;
  }

  getAccessToken(): string | null {
    return this.currentAdminSubject.value?.accessToken ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.currentAdminSubject.value;
  }
}
