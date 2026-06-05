import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, transition, style, animate } from '@angular/animations';
import { NotificationToastService, ToastNotification } from '../../../core/services/notification-toast.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div 
        *ngFor="let toast of toasts" 
        class="toast toast-{{toast.type}}"
        [@slideIn]
        (click)="handleToastClick(toast)"
        [class.clickable]="!!toast.onClick"
      >
        <div class="toast-header">
          <img 
            *ngIf="toast.avatarUrl" 
            [src]="toast.avatarUrl" 
            class="toast-avatar"
            alt="Avatar"
          />
          <div 
            *ngIf="!toast.avatarUrl && toast.type === 'message'" 
            class="toast-avatar-placeholder"
          >
            {{ toast.title.charAt(0).toUpperCase() }}
          </div>
          <strong>{{ toast.title }}</strong>
          <button class="toast-close" (click)="removeToast(toast.id); $event.stopPropagation()">
            ×
          </button>
        </div>
        <div class="toast-body">
          {{ toast.message }}
        </div>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 80px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-width: 400px;
    }

    .toast {
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      padding: 16px;
      min-width: 320px;
      border-left: 4px solid;
      cursor: default;
      transition: all 0.3s ease;
    }

    .toast.clickable {
      cursor: pointer;
    }

    .toast.clickable:hover {
      transform: translateX(-5px);
      box-shadow: 0 6px 25px rgba(0, 0, 0, 0.2);
    }

    .toast-message {
      border-left-color: #3b82f6;
    }

    .toast-success {
      border-left-color: #10b981;
    }

    .toast-error {
      border-left-color: #ef4444;
    }

    .toast-warning {
      border-left-color: #f59e0b;
    }

    .toast-info {
      border-left-color: #6366f1;
    }

    .toast-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;
    }

    .toast-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      object-fit: cover;
    }

    .toast-avatar-placeholder {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 600;
      font-size: 14px;
    }

    .toast-header strong {
      flex: 1;
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;
    }

    .toast-close {
      background: none;
      border: none;
      font-size: 24px;
      color: #9ca3af;
      cursor: pointer;
      padding: 0;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      transition: all 0.2s;
    }

    .toast-close:hover {
      background: #f3f4f6;
      color: #1f2937;
    }

    .toast-body {
      font-size: 13px;
      color: #4b5563;
      line-height: 1.5;
      padding-left: 42px;
    }

    @media (max-width: 640px) {
      .toast-container {
        right: 10px;
        left: 10px;
        max-width: none;
      }

      .toast {
        min-width: 0;
      }
    }
  `],
  animations: [
    trigger('slideIn', [
      transition(':enter', [
        style({ transform: 'translateX(400px)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ transform: 'translateX(400px)', opacity: 0 }))
      ])
    ])
  ]
})
export class ToastContainerComponent implements OnInit, OnDestroy {
  private readonly toastService = inject(NotificationToastService);
  
  protected toasts: ToastNotification[] = [];
  private subscription?: Subscription;
  private timers = new Map<string, number>();

  ngOnInit(): void {
    this.subscription = this.toastService.toast$.subscribe(toast => {
      this.addToast(toast);
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.timers.forEach(timer => clearTimeout(timer));
  }

  private addToast(toast: ToastNotification): void {
    this.toasts.push(toast);

    // Auto-suppression après la durée spécifiée
    if (toast.duration && toast.duration > 0) {
      const timer = window.setTimeout(() => {
        this.removeToast(toast.id);
      }, toast.duration);
      
      this.timers.set(toast.id, timer);
    }
  }

  protected removeToast(id: string): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
    
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
  }

  protected handleToastClick(toast: ToastNotification): void {
    if (toast.onClick) {
      toast.onClick();
      this.removeToast(toast.id);
    }
  }
}
