import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { finalize } from 'rxjs';
import { AdminDashboard, AdminDashboardService } from '../../core/admin-dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(AdminDashboardService);
  private readonly cd = inject(ChangeDetectorRef);

  dashboard: AdminDashboard | null = null;
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';
    this.dashboardService.getDashboard().pipe(
      finalize(() => {
        this.loading = false;
        this.cd.detectChanges();
      })
    ).subscribe({
      next: dashboard => {
        this.dashboard = dashboard;
        this.cd.detectChanges();
      },
      error: error => {
        this.errorMessage = error?.error?.error ?? 'Impossible de charger le dashboard.';
        this.cd.detectChanges();
      }
    });
  }

  ratio(value: number, total: number): number {
    return total === 0 ? 0 : Math.min(100, Math.round((value / total) * 100));
  }

  moderationTotal(): number {
    if (!this.dashboard) return 0;
    return this.dashboard.openUserReports + this.dashboard.openActivityReports;
  }
}
