import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  bio: string | null;
  avatarUrl: string | null;
  interests: string[];
  gallery: string[];
  visitedCountries: string[];
  facebook: string | null;
  instagram: string | null;
  whatsapp: string | null;
  profilePublic: boolean;
  createdAt: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  bio: string | null;
  facebook: string | null;
  instagram: string | null;
  whatsapp: string | null;
  visitedCountries: string[];
  profilePublic: boolean;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private apiUrl = '/api/users/me';

  constructor(private http: HttpClient) {}

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.apiUrl);
  }

  getProfileById(userId: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`http://localhost:8080/api/users/${userId}/profile`);
  }

  updateProfile(req: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(this.apiUrl, req);
  }

  updateInterests(interests: string[]): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/interests`, { interests });
  }

  uploadAvatar(file: File): Observable<UserProfile> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<UserProfile>(`${this.apiUrl}/avatar`, fd);
  }

  addGalleryPhoto(file: File): Observable<UserProfile> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<UserProfile>(`${this.apiUrl}/gallery`, fd);
  }

  removeGalleryPhoto(url: string): Observable<UserProfile> {
    return this.http.delete<UserProfile>(`${this.apiUrl}/gallery`, { params: { url } });
  }
}
