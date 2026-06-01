import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Badge {
  id: number;
  name: string;
  description: string;
  iconUrl: string;
  xpReward: number;
}

export interface LevelHistory {
  id: number;
  oldLevel: number;
  newLevel: number;
  achievedAt: string;
}

export interface GamificationProfile {
  level: number;
  xp: number;
  currentLevelXpRequired: number;
  totalXpEarned: number;
  achievements: Badge[];
  history: LevelHistory[];
}

export interface LeaderboardEntry {
  userId: number;
  name: string;
  avatarUrl: string | null;
  level: number;
  totalXpEarned: number;
}

@Injectable({ providedIn: 'root' })
export class GamificationService {
  private apiUrl = 'http://localhost:8080/api/gamification';

  constructor(private http: HttpClient) {}

  getGamificationProfile(): Observable<GamificationProfile> {
    return this.http.get<GamificationProfile>(`${this.apiUrl}/profile`);
  }

  getBadges(): Observable<Badge[]> {
    return this.http.get<Badge[]>(`${this.apiUrl}/badges`);
  }

  getLeaderboard(): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.apiUrl}/leaderboard`);
  }
}
