import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Search, Bell, User, Settings, HelpCircle } from 'lucide-angular';
import { AdminAuthService } from '../../core/admin-auth.service';
import { AdminDashboardService } from '../../core/admin-dashboard.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent implements OnInit {
  readonly authService = inject(AdminAuthService);
  private readonly dashboardService = inject(AdminDashboardService);
  private readonly cd = inject(ChangeDetectorRef);
  
  readonly Search = Search;
  readonly Bell = Bell;
  readonly User = User;
  readonly Settings = Settings;
  readonly HelpCircle = HelpCircle;

  notificationCount = 0;

  ngOnInit(): void {
    this.loadNotificationCount();
  }

  private loadNotificationCount(): void {
    this.dashboardService.getDashboard().subscribe({
      next: data => {
        this.notificationCount = data.openUserReports + data.openActivityReports;
        this.cd.detectChanges();
      },
      error: () => {
        this.notificationCount = 0;
      }
    });
  }
}
