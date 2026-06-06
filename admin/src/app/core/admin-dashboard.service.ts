import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';


export interface AuditEntry {
  id: number;
  adminEmail: string;
  action: string;
  targetType: string;
  targetId: number | null;
  details: string | null;
  createdAt: string;
}

export interface PlatformActivity {
  type: string;
  title: string;
  description: string;
  iconType: string;
  timestamp: string;
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
  recentActivities: PlatformActivity[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminDashboardService {
  private readonly apiUrl = `${environment.apiUrl}/api/auth`;

  private readonly apiUrl = '/api/admin/dashboard';

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(this.apiUrl);
  }
}
