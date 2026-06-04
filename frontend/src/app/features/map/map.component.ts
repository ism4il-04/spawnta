import { Component, OnDestroy, OnInit, ViewChild, NgZone, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { ActivityService, ActivityResponse } from '../../core/services/activity.service';
import { AuthService } from '../../core/services/auth.service';
import { GeocodingService, GeocodingResult } from '../../core/services/geocoding.service';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { CreateActivityComponent } from './create-activity/create-activity.component';
import { ActivityDetailComponent } from './activity-detail/activity-detail.component';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

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
    MatSliderModule,
    MatExpansionModule,
    MatSnackBarModule,
    FormsModule,
    CreateActivityComponent,
    ActivityDetailComponent
  ],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
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

  // Radius circle visualization
  radiusCircle: L.Circle | null = null;
  userLocation: L.LatLng | null = null;
  private radiusSubject = new Subject<number>();

  panelMode: 'CREATE' | 'DETAIL' | 'NONE' = 'NONE';
  selectedActivity: ActivityResponse | null = null;
  tempMarker: L.Marker | null = null;
  isFormVisible = true; // Nouveau: contrôle la visibilité du formulaire

  // Toggle states
  isSearchPanelCollapsed = false;

  private readonly activityDetailListener = (e: Event) => {
    const customEvent = e as CustomEvent<number>;
    const actId = customEvent.detail;
    const act = this.activities.find(a => a.id === actId);
    if (act) {
      this.openDetail(act);
    }
  };

  private readonly ngZone = inject(NgZone);
  protected readonly authService = inject(AuthService);

  constructor(
    private activityService: ActivityService,
    private geocodingService: GeocodingService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit() {
    this.locateUser(true); // silent initial load
    document.addEventListener('joinActivity', this.activityDetailListener as EventListener);

    // Setup debounced radius change
    this.radiusSubject.pipe(
      debounceTime(300)
    ).subscribe(() => {
      this.loadActivities();
    });
  }

  ngOnDestroy() {
    document.removeEventListener('joinActivity', this.activityDetailListener as EventListener);
    this.radiusSubject.complete();
  }

  onMapReady(map: L.Map) {
    this.map = map;

    // Refresh activities and update circle when map stops moving
    this.map.on('moveend', () => {
      this.ngZone.run(() => {
        this.updateRadiusCircle();
        this.loadActivities();
      });
    });

    // Handle clicks for activity creation
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.ngZone.run(() => {
        if (this.panelMode === 'CREATE') {
          const lat = e.latlng.lat;
          const lng = e.latlng.lng;

          if (this.tempMarker) {
            this.map.removeLayer(this.tempMarker);
          }

          // Use custom marker icon for temp marker too!
          const tempIcon = L.divIcon({
            className: 'custom-leaflet-marker-wrapper',
            html: `
              <div class="custom-leaflet-marker" style="background-color: #3b82f6; border-color: #3b82f644; transform: scale(1.1);">
                <i class="material-icons marker-icon" style="font-size: 16px; color: white;">add_location</i>
              </div>
            `,
            iconSize: [36, 36],
            iconAnchor: [18, 18]
          });

          this.tempMarker = L.marker([lat, lng], { icon: tempIcon }).addTo(this.map);

          if (this.createCmp) {
            this.createCmp.setLocation(lat, lng);
          }

          this.snackBar.open('Location selected on map.', 'OK', { duration: 2000 });
        }
      });
    });

    // Draw initial radius circle
    this.updateRadiusCircle();
  }

  locateUser(silent = false) {
    if (!silent) {
      this.snackBar.open('Acquiring GPS location...', 'Dismiss', { duration: 2000 });
    }

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.ngZone.run(() => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            this.userLocation = L.latLng(lat, lng);

            if (this.map && this.map.getPane('mapPane')) {
              this.map.setView([lat, lng], 13);
            } else {
              this.options.center = L.latLng(lat, lng);
            }

            if (!silent) {
              this.snackBar.open('Location centered!', 'Success', { duration: 3000 });
            }

            // Draw circle after map is ready
            setTimeout(() => this.updateRadiusCircle(), 100);
            this.loadActivities(lat, lng);
          });
        },
        (error) => {
          this.ngZone.run(() => {
            console.warn('Geolocation failed, using default location.');
            if (!silent) {
              this.snackBar.open('Unable to retrieve location. Using default.', 'OK', { duration: 3000 });
            }
            this.loadActivities();
          });
        }
      );
    } else {
      if (!silent) {
        this.snackBar.open('Geolocation not supported by your browser.', 'OK', { duration: 3000 });
      }
      this.loadActivities();
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
        this.snackBar.open('Failed to fetch nearby activities.', 'Retry', { duration: 3000 })
          .onAction().subscribe(() => this.loadActivities());
      }
    });
  }

  getMarkerIcon(category: string, type: 'MEETUP' | 'TRIP', isDestination = false): L.DivIcon {
    let color = '#0f766e'; // teal default
    let icon = 'groups'; // default meetup icon

    if (isDestination) {
      color = '#ef4444'; // red destination
      icon = 'flag';
    } else if (type === 'TRIP') {
      color = '#f97316'; // orange trip
      icon = 'explore';
    } else {
      // Map category to colors and icons
      switch (category) {
        case 'Coffee & Cafes':
          color = '#b45309'; // amber/brown
          icon = 'local_cafe';
          break;
        case 'Hiking & Trekking':
          color = '#15803d'; // green
          icon = 'terrain';
          break;
        case 'Nightlife':
          color = '#7e22ce'; // purple
          icon = 'nightlife';
          break;
        case 'Culture':
          color = '#1d4ed8'; // blue
          icon = 'museum';
          break;
        case 'Sports':
          color = '#be123c'; // rose
          icon = 'sports_soccer';
          break;
      }
    }

    return L.divIcon({
      className: 'custom-leaflet-marker-wrapper',
      html: `
        <div class="custom-leaflet-marker" style="background-color: ${color}; border-color: ${color}44;">
          <i class="material-icons marker-icon">${icon}</i>
        </div>
      `,
      iconSize: [36, 36],
      iconAnchor: [18, 18],
      popupAnchor: [0, -18]
    });
  }

  private buildPopupHtml(act: ActivityResponse, isDestination = false): string {
    const title = this.escapeHtml(act.title);
    const address = this.escapeHtml(act.address || 'Location TBA');
    const activityType = act.activityType === 'TRIP' ? 'Trip' : 'Meetup';
    const dateText = new Date(act.scheduledAt).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    }).replace(/^\w/, c => c.toUpperCase());
    const count = act.participantCount || 0;
    const maxParticipants = act.maxParticipants || '∞';
    const participantDisplay = maxParticipants === '∞' ? `${count}` : `${count}/${maxParticipants}`;

    return `
      <div class="modern-activity-popup">
        <h3 class="popup-title">${title}</h3>
        
        <div class="popup-location">
          <i class="material-icons">location_on</i>
          <span>${address}</span>
        </div>
        
        <div class="popup-info-row">
          <i class="material-icons">category</i>
          <span class="popup-type-badge">${activityType}</span>
        </div>
        
        <div class="popup-info-row">
          <i class="material-icons">event</i>
          <span>${dateText}</span>
        </div>
        
        <div class="popup-info-row">
          <i class="material-icons">people</i>
          <span>${participantDisplay} participant${count > 1 ? 's' : ''}</span>
        </div>
        
        <button class="popup-view-btn" onclick="document.dispatchEvent(new CustomEvent('joinActivity', {detail: ${act.id}}))">
          View Details
          <i class="material-icons">arrow_forward</i>
        </button>
      </div>
    `;
  }

  private escapeHtml(value: string): string {
    const div = document.createElement('div');
    div.textContent = value;
    return div.innerHTML;
  }

  updateMarkers() {
    this.layers = [];
    if (this.tempMarker) {
      this.layers.push(this.tempMarker);
    }

    this.activities.forEach(act => {
      const type = act.activityType || 'MEETUP';
      const category = act.category || '';

      if (type === 'MEETUP' && act.latitude && act.longitude) {
        const markerIcon = this.getMarkerIcon(category, 'MEETUP');
        const marker = L.marker([act.latitude, act.longitude], { icon: markerIcon });
        marker.bindPopup(this.buildPopupHtml(act));
        this.layers.push(marker);
      } else if (type === 'TRIP' && act.startLatitude && act.startLongitude) {
        const markerIcon = this.getMarkerIcon(category, 'TRIP');
        const marker = L.marker([act.startLatitude, act.startLongitude], { icon: markerIcon });
        marker.bindPopup(this.buildPopupHtml(act));
        this.layers.push(marker);

        if (act.destLatitude && act.destLongitude) {
          const destIcon = this.getMarkerIcon(category, 'TRIP', true);
          const destMarker = L.marker([act.destLatitude, act.destLongitude], { icon: destIcon });
          destMarker.bindPopup(this.buildPopupHtml(act, true));
          this.layers.push(destMarker);

          const line = L.polyline(
            [[act.startLatitude, act.startLongitude], [act.destLatitude, act.destLongitude]],
            { color: '#f97316', weight: 3, dashArray: '6, 8', opacity: 0.8 }
          );
          this.layers.push(line);
        }
      }
    });
  }

  searchLocation() {
    if (!this.searchQuery) return;
    this.geocodingService.search(this.searchQuery).subscribe({
      next: (results: GeocodingResult[]) => {
        this.searchResults = results;
        if (results.length > 0) {
          this.selectResult(results[0]);
        } else {
          this.snackBar.open('Location not found.', 'OK', { duration: 3000 });
        }
      },
      error: () => {
        this.snackBar.open('Failed to search location.', 'OK', { duration: 3000 });
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
    this.updateRadiusCircle();
    this.snackBar.open('Filters reset.', 'OK', { duration: 2000 });
  }

  selectResult(res: GeocodingResult) {
    const lat = parseFloat(res.lat);
    const lon = parseFloat(res.lon);
    if (this.map && this.map.getPane('mapPane')) {
      this.map.setView([lat, lon], 14);
    }
    this.searchResults = [];
    this.searchQuery = res.display_name;
  }

  onRadiusChange(newRadius: number) {
    this.filters.radiusKm = newRadius;
    this.updateRadiusCircle();
    this.radiusSubject.next(newRadius);
  }

  toggleSearchPanel() {
    this.isSearchPanelCollapsed = !this.isSearchPanelCollapsed;
  }

  private updateRadiusCircle() {
    if (this.radiusCircle && this.map) {
      this.map.removeLayer(this.radiusCircle);
      this.radiusCircle = null;
    }

    const centerPoint = this.userLocation || (this.map ? this.map.getCenter() : null);

    // Guard: map must exist and its overlay pane must be ready in the DOM
    if (!centerPoint || !this.map || !this.map.getPane('overlayPane')) {
      return;
    }

    const radiusMeters = this.filters.radiusKm * 1000;
    this.radiusCircle = L.circle(centerPoint, {
      radius: radiusMeters,
      color: '#0f766e', // match teal theme
      weight: 2,
      opacity: 0.5,
      fillColor: '#0f766e',
      fillOpacity: 0.05,
      dashArray: '6, 6'
    });

    this.radiusCircle.addTo(this.map);
  }

  openCreate() {
    this.panelMode = 'CREATE';
    this.isFormVisible = true; // Afficher le formulaire
    // Ne pas ouvrir le sidenav - le formulaire s'affichera au centre
    if (this.map) {
      this.map.getContainer().style.cursor = 'crosshair';
    }
    this.snackBar.open('Click anywhere on the map to set activity location.', 'Got it', { duration: 4000 });
  }

  openDetail(activity: ActivityResponse) {
    this.selectedActivity = activity;
    this.panelMode = 'DETAIL';
    this.sidenav.open();

    if (this.map) {
      this.map.getContainer().style.cursor = '';
    }
  }

  closePanel() {
    if (this.panelMode === 'DETAIL') {
      this.sidenav.close();
    }
    this.panelMode = 'NONE';
    this.selectedActivity = null;
    this.isFormVisible = true; // Réinitialiser la visibilité

    if (this.tempMarker && this.map) {
      this.map.removeLayer(this.tempMarker);
      this.tempMarker = null;
    }
    if (this.map) {
      this.map.getContainer().style.cursor = '';
    }
  }

  hideCreateForm() {
    this.isFormVisible = false;
    this.snackBar.open('Click on the map to select location, then click + to show form again.', 'OK', { duration: 3000 });
  }

  showCreateForm() {
    if (this.panelMode === 'CREATE') {
      this.isFormVisible = true;
    }
  }

  onActivityCreated(activity: any) {
    this.snackBar.open('🎉 Activity created successfully!', 'Hooray', { duration: 4000 });
    this.closePanel();
    this.loadActivities();
  }
}
