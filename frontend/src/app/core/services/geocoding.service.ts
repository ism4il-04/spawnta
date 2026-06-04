import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GeocodingResult {
  lat: string;
  lon: string;
  display_name: string;
}

@Injectable({
  providedIn: 'root'
})
export class GeocodingService {
  private readonly baseUrl = 'https://nominatim.openstreetmap.org';

  constructor(private http: HttpClient) {}

  search(query: string): Observable<GeocodingResult[]> {
    return this.http.get<GeocodingResult[]>(`${this.baseUrl}/search`, {
      params: {
        q: query,
        format: 'json',
        limit: '5',
        addressdetails: '1'
      }
    });
  }

  reverse(lat: number, lng: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/reverse`, {
      params: {
        lat: lat.toString(),
        lon: lng.toString(),
        format: 'json',
        addressdetails: '1'
      }
    });
  }
}
