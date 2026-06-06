import { CommonModule } from '@angular/common';
import { Component, HostListener, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { ChatService } from './core/services/chat.service';
import { NotificationsDropdownComponent } from './features/notifications/notifications-dropdown/notifications-dropdown';
import { MatIconModule } from '@angular/material/icon';
import { ToastContainerComponent } from './shared/components/toast-container/toast-container.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, MatIconModule, ToastContainerComponent, NotificationsDropdownComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App implements OnInit {
  protected readonly authService = inject(AuthService);
  protected readonly chatService = inject(ChatService);
  private readonly router = inject(Router);

  sidebarCollapsed = false;
  userMenuOpen = false;
  isLightTheme = false;

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('spawnta-theme');
    if (savedTheme === 'light') {
      this.isLightTheme = true;
      document.body.classList.add('light-theme');
    } else {
      this.isLightTheme = false;
      document.body.classList.remove('light-theme');
    }
  }

  toggleTheme(): void {
    this.isLightTheme = !this.isLightTheme;
    if (this.isLightTheme) {
      document.body.classList.add('light-theme');
      localStorage.setItem('spawnta-theme', 'light');
    } else {
      document.body.classList.remove('light-theme');
      localStorage.setItem('spawnta-theme', 'dark');
    }
  }

  get currentUser() {
    return this.authService.currentUserValue;
  }

  getInitials(): string {
    const user = this.currentUser;
    if (!user) return '?';
    const f = user.firstName?.charAt(0) ?? '';
    const l = user.lastName?.charAt(0) ?? '';
    return (f + l).toUpperCase() || user.email?.charAt(0).toUpperCase() || '?';
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  toggleUserMenu(): void {
    this.userMenuOpen = !this.userMenuOpen;
  }

  navigateToProfile(): void {
    this.userMenuOpen = false;
    this.router.navigate(['/profile']);
  }

  navigateToSubscription(): void {
    this.userMenuOpen = false;
    this.router.navigate(['/subscription']);
  }

  logout(): void {
    this.authService.logout();
    this.userMenuOpen = false;
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu-wrapper')) {
      this.userMenuOpen = false;
    }
  }
}
