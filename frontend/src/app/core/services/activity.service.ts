import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ActivityResponse {
  id: number;
  title: string;
  description: string;
  activityType: 'MEETUP' | 'TRIP';
  participationMode: 'DIRECT' | 'APPROVAL';
  maxParticipants: number;
  scheduledAt: string;
  durationMinutes: number;
  category: string;
  latitude: number;
  longitude: number;
  startLatitude: number;
  startLongitude: number;
  destLatitude: number;
  destLongitude: number;
  address: string;
  hostId: number;
  hostFirstName: string;
  hostLastName: string;
  hostEmail: string;
  participantCount: number;
  createdAt: string;
}

export interface ActivityFilters {
  radiusKm?: number;
  category?: string;
  participationMode?: 'DIRECT' | 'APPROVAL' | '';
  activityType?: 'MEETUP' | 'TRIP' | '';
  scheduledDate?: string;
}

export interface ActivityParticipantResponse {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  status: 'PENDING' | 'APPROVED' | 'DECLINED';
  introMessage: string | null;
  joinedAt: string;
}

export interface CreateActivityRequest {
  title: string;
  description: string;
  activityType: 'MEETUP' | 'TRIP';
  participationMode: 'DIRECT' | 'APPROVAL';
  maxParticipants?: number | null;
  scheduledAt: string;
  durationMinutes?: number | null;
  category?: string;
  latitude?: number;
  longitude?: number;
  startLatitude?: number;
  startLongitude?: number;
  destLatitude?: number;
  destLongitude?: number;
  address?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  private apiUrl = 'http://localhost:8080/api/activities';

  constructor(private http: HttpClient) {}

  getNearby(lat: number, lng: number, filters: ActivityFilters = {}): Observable<ActivityResponse[]> {
    let params = new HttpParams()
      .set('lat', lat.toString())
      .set('lng', lng.toString())
      .set('radiusKm', (filters.radiusKm ?? 10).toString());

    if (filters.category) {
      params = params.set('category', filters.category);
    }
    if (filters.participationMode) {
      params = params.set('participationMode', filters.participationMode);
    }
    if (filters.activityType) {
      params = params.set('activityType', filters.activityType);
    }
    if (filters.scheduledDate) {
      params = params.set('scheduledDate', filters.scheduledDate);
    }
    
    return this.http.get<ActivityResponse[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<ActivityResponse> {
    return this.http.get<ActivityResponse>(`${this.apiUrl}/${id}`);
  }

  create(data: CreateActivityRequest): Observable<ActivityResponse> {
    return this.http.post<ActivityResponse>(this.apiUrl, data);
  }

  join(id: number, introMessage?: string): Observable<any> {
    const body = introMessage ? { introMessage } : {};
    return this.http.post(`${this.apiUrl}/${id}/join`, body);
  }

  approveParticipant(activityId: number, participantId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${activityId}/participants/${participantId}/approve`, {});
  }

  getPendingParticipants(activityId: number): Observable<ActivityParticipantResponse[]> {
    return this.http.get<ActivityParticipantResponse[]>(`${this.apiUrl}/${activityId}/participants/pending`);
  }

  getMyActivities(): Observable<MyActivityResponse[]> {
    return this.http.get<MyActivityResponse[]>(`${this.apiUrl}/my`);
  }
}

export interface MyActivityResponse {
  activity: ActivityResponse;
  participation: {
    host: boolean;
    joined: boolean;
    pendingRequest: boolean;
    canCheckIn: boolean;
    canRate: boolean;
    hasRated: boolean;
    attendanceStatus: string | null;
  };
}
