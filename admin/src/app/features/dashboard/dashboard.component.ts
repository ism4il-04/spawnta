import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
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

  dashboard: AdminDashboard | null = null;
  loading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';
    this.dashboardService.getDashboard().pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: dashboard => this.dashboard = dashboard,
      error: error => this.errorMessage = error?.error?.error ?? 'Impossible de charger le dashboard.'
    });
  }
}
