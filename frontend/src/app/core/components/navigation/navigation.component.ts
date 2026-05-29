import { Component, Input, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil, Observable, of } from 'rxjs';
import { AuthService } from '../../services/auth.service';

// Mock services - replace with actual implementations
interface ChatService {
  unreadCount$: Observable<number>;
}

interface User {
  firstName: string;
  lastName: string;
  email: string;
  avatarUrl?: string;
}

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatIconModule
  ],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent implements OnInit, OnDestroy {
  @Input() showSearch = true;

  // State
  searchQuery = '';
  userMenuOpen = false;
  mobileMenuOpen = false;
  notificationCount = 0;
  hasNotifications = false;
  currentUser: User | null = null;

  // Mock chat service - replace with actual service
  chatService: ChatService = {
    unreadCount$: of(0)
  };

  private destroy$ = new Subject<void>();

  constructor(
    public authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    // Subscribe to auth state changes
    this.authService.currentUser$.pipe(
      takeUntil(this.destroy$)
    ).subscribe((user: any) => {
      this.currentUser = user;
    });

    // Mock notification count - replace with actual service
    this.notificationCount = 3;
    this.hasNotifications = this.notificationCount > 0;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.userMenuOpen = false;
    }
  }

  // Search
  onSearch(event: Event) {
    const target = event.target as HTMLInputElement;
    this.searchQuery = target.value;
    // Implement search logic here
    console.log('Searching for:', this.searchQuery);
  }

  // User Menu
  toggleUserMenu() {
    this.userMenuOpen = !this.userMenuOpen;
  }

  closeUserMenu() {
    this.userMenuOpen = false;
  }

  getInitials(): string {
    if (!this.currentUser) return 'U';
    const first = this.currentUser.firstName?.charAt(0) || '';
    const last = this.currentUser.lastName?.charAt(0) || '';
    return (first + last).toUpperCase() || 'U';
  }

  // Notifications
  toggleNotifications() {
    // Implement notifications panel toggle
    console.log('Toggle notifications');
  }

  // Mobile Menu
  toggleMobileMenu() {
    this.mobileMenuOpen = !this.mobileMenuOpen;
    // Prevent body scroll when mobile menu is open
    if (this.mobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }

  closeMobileMenu() {
    this.mobileMenuOpen = false;
    document.body.style.overflow = '';
  }

  // Auth
  logout() {
    this.authService.logout();
    this.closeUserMenu();
    this.closeMobileMenu();
    this.snackBar.open('Déconnexion réussie', 'Fermer', { duration: 3000 });
    this.router.navigate(['/']);
  }
}