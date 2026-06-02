import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ActivityReportAdmin, AdminModeration, AdminModerationService, UserReportAdmin } from '../../core/admin-moderation.service';

@Component({
  selector: 'app-moderation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './moderation.component.html',
  styleUrls: ['./moderation.component.scss']
})
export class ModerationComponent implements OnInit {
  private readonly moderationService = inject(AdminModerationService);

  data: AdminModeration | null = null;
  loading = true;
  actionId = '';
  errorMessage = '';
  status = 'all';

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.errorMessage = '';
    this.moderationService.getReports(this.status).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: data => this.data = data,
      error: error => this.errorMessage = error?.error?.error ?? 'Impossible de charger la moderation.'
    });
  }

  updateUserReport(report: UserReportAdmin, action: 'investigate' | 'resolve' | 'dismiss'): void {
    const notes = window.prompt('Notes de moderation', action === 'dismiss' ? 'Signalement rejete' : 'Decision admin');
    if (notes === null) return;
    this.actionId = `user-${report.id}`;
    this.moderationService.updateUserReport(report.id, action, notes).pipe(
      finalize(() => this.actionId = '')
    ).subscribe({
      next: () => this.loadReports(),
      error: error => this.errorMessage = error?.error?.error ?? 'Action impossible.'
    });
  }

  updateActivityReport(report: ActivityReportAdmin, action: 'investigate' | 'resolve' | 'dismiss'): void {
    const notes = window.prompt('Notes de moderation', action === 'dismiss' ? 'Signalement rejete' : 'Decision admin');
    if (notes === null) return;
    this.actionId = `activity-${report.id}`;
    this.moderationService.updateActivityReport(report.id, action, notes).pipe(
      finalize(() => this.actionId = '')
    ).subscribe({
      next: () => this.loadReports(),
      error: error => this.errorMessage = error?.error?.error ?? 'Action impossible.'
    });
  }
}
