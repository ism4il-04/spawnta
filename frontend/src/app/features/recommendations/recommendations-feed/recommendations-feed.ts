import { Component, OnInit, inject, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RecommendationService, FeedActivity } from '../../../core/services/recommendation.service';

@Component({
  selector: 'app-recommendations-feed',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatProgressSpinnerModule],
  templateUrl: './recommendations-feed.html',
  styleUrl: './recommendations-feed.scss'
})
export class RecommendationsFeedComponent implements OnInit {
  private recService = inject(RecommendationService);
  private snackBar = inject(MatSnackBar);
  private ngZone = inject(NgZone);
  private router = inject(Router);

  feed: FeedActivity[] = [];
  loading = false;
  feedError = false;

  ngOnInit(): void {
    this.loadFeed();
  }

  loadFeed(): void {
    this.loading = true;
    this.feedError = false;
    this.recService.getPersonalizedFeed().subscribe({
      next: (f) => {
        this.feed = f;
        this.loading = false;
        if (f.length === 0) {
          this.generateRecommendationsThenReload();
        }
      },
      error: () => {
        this.loading = false;
        this.feedError = true;
      }
    });
  }

  /** Feed is built from generated recommendations — run generate once when empty */
  private generateRecommendationsThenReload(): void {
    this.loading = true;
    if (!navigator.geolocation) {
      this.runGeneration(null, null);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.ngZone.run(() => {
          this.runGeneration(pos.coords.latitude, pos.coords.longitude);
        });
      },
      () => {
        this.ngZone.run(() => {
          this.snackBar.open('Allow geolocation for better recommendations', 'OK', { duration: 3000 });
          this.runGeneration(null, null);
        });
      }
    );
  }

  private runGeneration(lat: number | null, lng: number | null): void {
    this.recService.generateRecommendations(lat, lng).subscribe({
      next: () => {
        this.recService.getPersonalizedFeed().subscribe({
          next: (f) => {
            this.feed = f;
            this.loading = false;
          },
          error: () => {
            this.loading = false;
            this.feedError = true;
          }
        });
      },
      error: () => {
        this.loading = false;
        this.feedError = true;
        this.snackBar.open('Unable to generate recommendations', 'Close', { duration: 4000 });
      }
    });
  }

  onActivityClick(item: FeedActivity): void {
    // Track click
    this.recService.trackClick(item.recommendationId).subscribe();
    // Navigate to map with activityId
    this.router.navigate(['/map'], { queryParams: { activityId: item.activityId } });
  }

  refresh(): void {
    this.generateRecommendationsThenReload();
  }
}
