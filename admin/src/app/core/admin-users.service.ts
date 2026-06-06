import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface AdminUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ADMIN' | string;
  emailVerified: boolean;
  subscriptionTier: string;
  premium: boolean;
  banned: boolean;
  suspendedUntil: string | null;
  suspensionReason: string | null;
  level: number;
  xp: number;
  createdAt: string;
}

export interface AdminUsersSummary {
  totalUsers: number;
  admins: number;
  premiumUsers: number;
  bannedUsers: number;
  suspendedUsers: number;
  unverifiedUsers: number;
}

export interface AdminUsersResponse {
  users: AdminUser[];
  summary: AdminUsersSummary;
}

export interface UserFilters {
  search?: string;
  status?: string;
  tier?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminUsersService {
  private readonly apiUrl = '/api/admin/users';

  constructor(private http: HttpClient) {}

  getUsers(filters: UserFilters): Observable<AdminUsersResponse> {
    let params = new HttpParams();
    if (filters.search) params = params.set('search', filters.search);
    if (filters.status && filters.status !== 'all') params = params.set('status', filters.status);
    if (filters.tier && filters.tier !== 'all') params = params.set('tier', filters.tier);
    return this.http.get<AdminUsersResponse>(this.apiUrl, { params });
  }

  banUser(id: number, reason: string): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.apiUrl}/${id}/ban`, { reason });
  }

  suspendUser(id: number, reason: string, days: number): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.apiUrl}/${id}/suspend`, { reason, days });
  }

  restoreUser(id: number): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.apiUrl}/${id}/restore`, {});
  }

  updateRole(id: number, role: string): Observable<AdminUser> {
    let params = new HttpParams().set('role', role);
    return this.http.patch<AdminUser>(`${this.apiUrl}/${id}/role`, {}, { params });
  }
}
