import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface AdminActivity {
  id: number;
  title: string;
  description: string | null;
  activityType: 'MEETUP' | 'TRIP';
  participationMode: 'DIRECT' | 'APPROVAL';
  category: string | null;
  maxParticipants: number | null;
  participantCount: number;
  scheduledAt: string;
  createdAt: string;
  hostId: number;
  hostEmail: string;
  hostName: string;
}

export interface AdminActivitiesResponse {
  activities: AdminActivity[];
  totalActivities: number;
  upcomingActivities: number;
  pastActivities: number;
}

export interface AdminActivitiesFilters {
  search?: string;
  status?: string;
  category?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminActivitiesService {
  private readonly apiUrl = '/api/admin/activities';

  constructor(private http: HttpClient) {}

  getActivities(filters: AdminActivitiesFilters = {}): Observable<AdminActivitiesResponse> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value && value !== 'all') {
        params = params.set(key, value);
      }
    });
    return this.http.get<AdminActivitiesResponse>(this.apiUrl, { params });
  }

  deleteActivity(id: number, reason: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      body: { reason }
    });
  }
}
