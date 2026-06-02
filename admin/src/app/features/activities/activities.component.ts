import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AdminActivitiesResponse,
  AdminActivitiesService,
  AdminActivity
} from '../../core/admin-activities.service';

@Component({
  selector: 'app-activities',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './activities.component.html',
  styleUrls: ['./activities.component.scss']
})
export class ActivitiesComponent implements OnInit {
  private readonly activitiesService = inject(AdminActivitiesService);

  activities: AdminActivity[] = [];
  summary: Omit<AdminActivitiesResponse, 'activities'> | null = null;
  loading = true;
  actionActivityId: number | null = null;
  errorMessage = '';

  search = '';
  status = 'all';
  category = 'all';

  ngOnInit(): void {
    this.loadActivities();
  }

  loadActivities(): void {
    this.loading = true;
    this.errorMessage = '';
    this.activitiesService.getActivities({
      search: this.search.trim(),
      status: this.status,
      category: this.category
    }).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: response => {
        this.activities = response.activities;
        this.summary = {
          totalActivities: response.totalActivities,
          upcomingActivities: response.upcomingActivities,
          pastActivities: response.pastActivities
        };
      },
      error: error => {
        this.errorMessage = error?.error?.error ?? 'Impossible de charger les activites.';
      }
    });
  }

  deleteActivity(activity: AdminActivity): void {
    const reason = window.prompt(`Raison de suppression pour "${activity.title}"`, 'Suppression moderation');
    if (reason === null) return;
    this.actionActivityId = activity.id;
    this.activitiesService.deleteActivity(activity.id, reason).pipe(
      finalize(() => this.actionActivityId = null)
    ).subscribe({
      next: () => this.loadActivities(),
      error: error => this.errorMessage = error?.error?.error ?? 'Suppression impossible.'
    });
  }

  typeLabel(activity: AdminActivity): string {
    return activity.activityType === 'TRIP' ? 'Trip' : 'Meetup';
  }

  statusLabel(activity: AdminActivity): string {
    return new Date(activity.scheduledAt).getTime() > Date.now() ? 'A venir' : 'Passee';
  }

  statusClass(activity: AdminActivity): string {
    return new Date(activity.scheduledAt).getTime() > Date.now() ? 'success' : 'muted';
  }

  trackById(_: number, activity: AdminActivity): number {
    return activity.id;
  }
}
