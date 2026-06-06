import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
export interface UserReportAdmin {
  id: number;
  status: string;
  reason: string;
  description: string;
  reporterEmail: string;
  reportedUserId: number;
  reportedUserEmail: string;
  reportedUserName: string;
  resolutionNotes: string | null;
  resolvedByEmail: string | null;
  createdAt: string;
  resolvedAt: string | null;
}

export interface ActivityReportAdmin {
  id: number;
  status: string;
  reason: string;
  description: string;
  reporterEmail: string;
  activityId: number;
  activityTitle: string;
  activityDescription: string;
  hostEmail: string;
  resolutionNotes: string | null;
  resolvedByEmail: string | null;
  createdAt: string;
  resolvedAt: string | null;
}

export interface AdminModeration {
  openUserReports: number;
  investigatingUserReports: number;
  resolvedUserReports: number;
  dismissedUserReports: number;
  openActivityReports: number;
  investigatingActivityReports: number;
  resolvedActivityReports: number;
  dismissedActivityReports: number;
  userReports: UserReportAdmin[];
  activityReports: ActivityReportAdmin[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminModerationService {
  private readonly apiUrl = '/api/admin/moderation';

  private readonly apiUrl = `${environment.apiUrl}/api/auth`;
  constructor(private http: HttpClient) {}

  getReports(status = 'all'): Observable<AdminModeration> {
    const params = new HttpParams().set('status', status);
    return this.http.get<AdminModeration>(`${this.apiUrl}/reports`, { params });
  }

  updateUserReport(id: number, action: 'investigate' | 'resolve' | 'dismiss', notes: string): Observable<UserReportAdmin> {
    return this.http.patch<UserReportAdmin>(`${this.apiUrl}/user-reports/${id}/${action}`, { notes });
  }

  updateActivityReport(id: number, action: 'investigate' | 'resolve' | 'dismiss', notes: string): Observable<ActivityReportAdmin> {
    return this.http.patch<ActivityReportAdmin>(`${this.apiUrl}/activity-reports/${id}/${action}`, { notes });
  }
}
