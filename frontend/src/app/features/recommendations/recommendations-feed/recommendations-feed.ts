import { Component, OnInit, inject, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RecommendationService, FeedActivity } from '../../../core/services/recommendation.service';

@Component({
  selector: 'app-recommendations-feed',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  templateUrl: './recommendations-feed.html',
  styleUrl: './recommendations-feed.scss'
})
export class RecommendationsFeedComponent implements OnInit {
  private recService = inject(RecommendationService);
  private snackBar = inject(MatSnackBar);
  private ngZone = inject(NgZone);

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
        if (f.length) {
          this.feed = f;
          this.loading = false;
        } else {
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
    if (!navigator.geolocation) {
      this.loading = false;
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.ngZone.run(() => {
          this.recService.generateRecommendations(pos.coords.latitude, pos.coords.longitude).subscribe({
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
        });
      },
      () => {
        this.ngZone.run(() => {
          this.loading = false;
          this.snackBar.open('Allow geolocation for personalized recommendations', 'Close', { duration: 4000 });
        });
      }
    );
  }

  refresh(): void {
    this.generateRecommendationsThenReload();
  }
}
