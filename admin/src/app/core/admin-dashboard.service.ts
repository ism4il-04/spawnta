import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface AuditEntry {
  id: number;
  adminEmail: string;
  action: string;
  targetType: string;
  targetId: number | null;
  details: string | null;
  createdAt: string;
}

export interface AdminDashboard {
  totalUsers: number;
  admins: number;
  premiumUsers: number;
  bannedUsers: number;
  suspendedUsers: number;
  unverifiedUsers: number;
  totalActivities: number;
  upcomingActivities: number;
  openUserReports: number;
  openActivityReports: number;
  subscriptionPlans: number;
  activeSubscriptions: number;
  successfulPayments: number;
  recentAuditLogs: AuditEntry[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminDashboardService {
  private readonly apiUrl = 'http://localhost:8080/api/admin/dashboard';

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(this.apiUrl);
  }
}
