import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Search, Bell, User, Settings, HelpCircle, Sun, Moon } from 'lucide-angular';
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
  readonly Sun = Sun;
  readonly Moon = Moon;

  notificationCount = 0;
  isDarkTheme = false;

  toggleTheme(): void {
    this.isDarkTheme = !this.isDarkTheme;
    if (this.isDarkTheme) {
      document.body.classList.add('dark-theme');
      localStorage.setItem('spawnta-admin-theme', 'dark');
    } else {
      document.body.classList.remove('dark-theme');
      localStorage.setItem('spawnta-admin-theme', 'light');
    }
  }

  ngOnInit(): void {
    this.loadNotificationCount();
    this.isDarkTheme = document.body.classList.contains('dark-theme');
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
