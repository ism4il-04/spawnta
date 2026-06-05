import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, TopbarComponent],
  template: `
    <div class="admin-layout-container">
      <app-sidebar [(collapsed)]="isSidebarCollapsed"></app-sidebar>
      <div class="main-content-wrapper">
        <app-topbar></app-topbar>
        <main class="page-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .admin-layout-container {
      display: flex;
      min-height: 100vh;
      background: var(--background);
    }

    .main-content-wrapper {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .page-content {
      padding: 2rem;
      flex: 1;
    }
  `]
})
export class AdminLayoutComponent {
  isSidebarCollapsed = false;
}
