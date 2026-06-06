import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Recommendation {
  id: number;
  score: number;
  reason: string;
  activityId: number;
  title: string;
  category: string;
  scheduledAt: string;
  hostName: string;
}

export interface FeedActivity {
  recommendationId: number;
  activityId: number;
  title: string;
  description: string;
  category: string;
  scheduledAt: string;
  hostName: string;
  hostAvatarUrl: string;
  reason: string;
}

export interface UserNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  relatedActivityId: any;
  relatedUserId: any;
}

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) { }

  generateRecommendations(latitude?: number | null, longitude?: number | null): Observable<Recommendation[]> {
    return this.http.post<Recommendation[]>(`${this.apiUrl}/recommendations/generate`, { latitude, longitude });
  }

  getPersonalizedFeed(): Observable<FeedActivity[]> {
    return this.http.get<FeedActivity[]>(`${this.apiUrl}/recommendations/feed`);
  }

  trackClick(recommendationId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/recommendations/${recommendationId}/click`, {});
  }

  getNotifications(): Observable<UserNotification[]> {
    return this.http.get<UserNotification[]>(`${this.apiUrl}/notifications`);
  }

  getUnreadCount(): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.apiUrl}/notifications/unread/count`);
  }

  markAsRead(notificationId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/notifications/${notificationId}/read`, {});
  }
}
