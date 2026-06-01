import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { ToastrService } from 'ngx-toastr';
import { Subscription, interval } from 'rxjs';
import { RecommendationService, UserNotification } from '../../../core/services/recommendation.service';

@Component({
  selector: 'app-notifications-dropdown',
  standalone: true,
  imports: [CommonModule, RouterLink, MatMenuModule, MatButtonModule, MatIconModule, MatBadgeModule, MatDividerModule],
  templateUrl: './notifications-dropdown.html',
  styleUrl: './notifications-dropdown.scss'
})
export class NotificationsDropdownComponent implements OnInit, OnDestroy {
  private recService = inject(RecommendationService);
  private toastr = inject(ToastrService);

  notifications: UserNotification[] = [];
  unreadCount = 0;
  private pollSub?: Subscription;
  private baselineSet = false;

  ngOnInit(): void {
    this.loadNotifications();
    this.pollSub = interval(30000).subscribe(() => this.pollForNewNotifications());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  loadNotifications(): void {
    this.recService.getNotifications().subscribe({
      next: (n) => {
        this.notifications = n;
        this.unreadCount = n.filter(x => !x.isRead).length;
        this.baselineSet = true;
      },
      error: () => {
        // API may be down — keep dropdown usable
      }
    });
  }

  private pollForNewNotifications(): void {
    this.recService.getUnreadCount().subscribe({
      next: ({ unreadCount }) => {
        if (this.baselineSet && unreadCount > this.unreadCount) {
          this.recService.getNotifications().subscribe(n => {
            const newest = n.find(x => !x.isRead);
            if (newest) {
              this.toastr.info(newest.message || '', newest.title || 'Notification', {
                timeOut: 6000,
                closeButton: true
              });
            }
            this.notifications = n;
            this.unreadCount = unreadCount;
          });
        } else {
          this.unreadCount = unreadCount;
        }
      }
    });
  }

  markAsRead(id: number): void {
    const notif = this.notifications.find(n => n.id === id);
    if (notif && !notif.isRead) {
      notif.isRead = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.recService.markAsRead(id).subscribe();
    }
  }

  getIcon(type: string): string {
    switch (type) {
      case 'FRIEND_ACTIVITY': return 'group';
      case 'TRENDING': return 'trending_up';
      case 'PERSONAL': return 'person';
      default: return 'notifications';
    }
  }
}
