import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { ActivityService, ActivityResponse } from '../../core/services/activity.service';
import { GeocodingService, GeocodingResult } from '../../core/services/geocoding.service';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { CreateActivityComponent } from './create-activity/create-activity.component';
import { ActivityDetailComponent } from './activity-detail/activity-detail.component';

// Fix Leaflet icons issue
const iconRetinaUrl = 'assets/marker-icon.svg';
const iconUrl = 'assets/marker-icon.svg';
const shadowUrl = 'assets/marker-shadow.svg';
const iconDefault = L.icon({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [
    CommonModule, 
    LeafletModule, 
    MatSidenavModule, 
    MatButtonModule, 
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    CreateActivityComponent,
    ActivityDetailComponent
  ],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements OnInit, OnDestroy {
  @ViewChild('sidenav') sidenav!: MatSidenav;
  @ViewChild('createCmp') createCmp!: CreateActivityComponent;

  map!: L.Map;
  options: L.MapOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18, attribution: '© OpenStreetMap' })
    ],
    zoom: 13,
    center: L.latLng(48.8566, 2.3522) // Paris default
  };
  
  activities: ActivityResponse[] = [];
  markers: L.Marker[] = [];
  layers: L.Layer[] = [];

  searchQuery = '';
  searchResults: GeocodingResult[] = [];
  filters = {
    radiusKm: 50,
    category: '',
    participationMode: '' as '' | 'DIRECT' | 'APPROVAL',
    activityType: '' as '' | 'MEETUP' | 'TRIP',
    scheduledDate: ''
  };
  readonly categories = ['Coffee & Cafes', 'Hiking & Trekking', 'Nightlife', 'Culture', 'Sports'];

  panelMode: 'CREATE' | 'DETAIL' | 'NONE' = 'NONE';
  selectedActivity: ActivityResponse | null = null;
  tempMarker: L.Marker | null = null;
  private readonly activityDetailListener = (e: Event) => {
    const customEvent = e as CustomEvent<number>;
    const actId = customEvent.detail;
    const act = this.activities.find(a => a.id === actId);
    if (act) {
      this.openDetail(act);
    }
  };

  constructor(
    private activityService: ActivityService,
    private geocodingService: GeocodingService
  ) {}

  ngOnInit() {
    this.locateUser();
    document.addEventListener('joinActivity', this.activityDetailListener as EventListener);
  }

  ngOnDestroy() {
    document.removeEventListener('joinActivity', this.activityDetailListener as EventListener);
  }

  onMapReady(map: L.Map) {
    this.map = map;
    
    // Refresh activities when map stops moving
    this.map.on('moveend', () => {
      this.loadActivities();
    });

    // Handle clicks for activity creation
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      if (this.panelMode === 'CREATE') {
        const lat = e.latlng.lat;
        const lng = e.latlng.lng;
        
        if (this.tempMarker) {
          this.map.removeLayer(this.tempMarker);
        }
        this.tempMarker = L.marker([lat, lng]).addTo(this.map);
        
        if (this.createCmp) {
          this.createCmp.setLocation(lat, lng);
        }
      }
    });
  }

  locateUser() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;
          if (this.map) {
            this.map.setView([lat, lng], 13);
          } else {
            this.options.center = L.latLng(lat, lng);
          }
          this.loadActivities(lat, lng);
        },
        (error) => {
          console.warn('Geolocation failed, using default location.');
          this.loadActivities();
        }
      );
    }
  }

  loadActivities(lat?: number, lng?: number) {
    if (lat == null || lng == null) {
      if (!this.map) return;
      const center = this.map.getCenter();
      lat = center.lat;
      lng = center.lng;
    }
    
    this.activityService.getNearby(lat, lng, this.filters).subscribe({
      next: (data: ActivityResponse[]) => {
        this.activities = data;
        this.updateMarkers();
      },
      error: (err: any) => {
        console.error('Failed to load activities', err);
      }
    });
  }

  updateMarkers() {
    this.layers = [];
    if (this.tempMarker) {
      this.layers.push(this.tempMarker); // keep temp marker if exists
    }

    this.activities.forEach(act => {
      if (act.activityType === 'MEETUP' && act.latitude && act.longitude) {
        const marker = L.marker([act.latitude, act.longitude]);
        marker.bindPopup(`<b>${act.title}</b><br>${act.category || 'Meetup'}<br><button class="join-btn" onclick="document.dispatchEvent(new CustomEvent('joinActivity', {detail: ${act.id}}))">Details</button>`);
        this.layers.push(marker);
      } else if (act.activityType === 'TRIP' && act.startLatitude && act.startLongitude) {
        const marker = L.marker([act.startLatitude, act.startLongitude]);
        marker.bindPopup(`<b>${act.title} (Trip Start)</b><br>${act.category || 'Trip'}<br><button class="join-btn" onclick="document.dispatchEvent(new CustomEvent('joinActivity', {detail: ${act.id}}))">Details</button>`);
        this.layers.push(marker);
        
        if (act.destLatitude && act.destLongitude) {
          const destMarker = L.marker([act.destLatitude, act.destLongitude]);
          destMarker.bindPopup(`<b>${act.title} (Destination)</b>`);
          this.layers.push(destMarker);
          
          const line = L.polyline([[act.startLatitude, act.startLongitude], [act.destLatitude, act.destLongitude]], {color: 'red'});
          this.layers.push(line);
        }
      }
    });
  }

  searchLocation() {
    if (!this.searchQuery) return;
    this.geocodingService.search(this.searchQuery).subscribe((results: GeocodingResult[]) => {
      this.searchResults = results;
      if (results.length > 0) {
        this.selectResult(results[0]);
      }
    });
  }

  applyFilters() {
    this.loadActivities();
  }

  resetFilters() {
    this.filters = {
      radiusKm: 50,
      category: '',
      participationMode: '',
      activityType: '',
      scheduledDate: ''
    };
    this.loadActivities();
  }

  selectResult(res: GeocodingResult) {
    const lat = parseFloat(res.lat);
    const lon = parseFloat(res.lon);
    this.map.setView([lat, lon], 14);
    this.searchResults = [];
    this.searchQuery = res.display_name;
  }

  openCreate() {
    this.panelMode = 'CREATE';
    this.sidenav.open();
    // Enable crosshair cursor on map
    if (this.map) {
      this.map.getContainer().style.cursor = 'crosshair';
    }
  }

  openDetail(activity: ActivityResponse) {
    this.selectedActivity = activity;
    this.panelMode = 'DETAIL';
    this.sidenav.open();
    
    // reset cursor
    if (this.map) {
      this.map.getContainer().style.cursor = '';
    }
  }

  closePanel() {
    this.sidenav.close();
    this.panelMode = 'NONE';
    this.selectedActivity = null;
    
    if (this.tempMarker && this.map) {
      this.map.removeLayer(this.tempMarker);
      this.tempMarker = null;
    }
    if (this.map) {
      this.map.getContainer().style.cursor = '';
    }
  }

  onActivityCreated(activity: any) {
    alert('Activity Created!');
    this.closePanel();
    this.loadActivities();
  }
}
