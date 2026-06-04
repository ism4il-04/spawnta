import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CheckInResult {
  attendanceId: number;
  qrCode: string;
  activityName: string;
  checkInDeadline: string;
}

export interface ActivityStats {
  averageRating: number;
  totalReviews: number;
}

export interface ParticipationStatus {
  host: boolean;
  joined: boolean;
  pendingRequest: boolean;
  canCheckIn: boolean;
  canRate: boolean;
  hasRated: boolean;
  attendanceStatus: string | null;
}

export interface RatingEntry {
  id: number;
  ratingScore: number;
  comment: string;
  raterName: string;
  raterAvatarUrl: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getMyParticipationStatus(activityId: number): Observable<ParticipationStatus> {
    return this.http.get<ParticipationStatus>(
      `${this.apiUrl}/activities/${activityId}/attendance/me`
    );
  }

  initiateCheckIn(activityId: number, latitude: number, longitude: number): Observable<CheckInResult> {
    return this.http.post<CheckInResult>(
      `${this.apiUrl}/activities/${activityId}/attendance/check-in/initiate`,
      { latitude, longitude }
    );
  }

  confirmCheckIn(activityId: number, photoUrl: string, latitude: number, longitude: number): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/activities/${activityId}/attendance/check-in/confirm`,
      { photoUrl, latitude, longitude }
    );
  }

  hostConfirmAttendance(activityId: number, participantIds: number[]): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/activities/${activityId}/attendance/host-confirm`,
      { participantIds }
    );
  }

  rateActivity(activityId: number, ratingScore: number, comment: string): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/activities/${activityId}/ratings`,
      { ratingScore, comment }
    );
  }

  getActivityStats(activityId: number): Observable<ActivityStats> {
    return this.http.get<ActivityStats>(`${this.apiUrl}/activities/${activityId}/ratings/stats`);
  }

  getActivityRatings(activityId: number): Observable<RatingEntry[]> {
    return this.http.get<RatingEntry[]>(`${this.apiUrl}/activities/${activityId}/ratings`);
  }
}
