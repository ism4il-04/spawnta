import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ToastNotification {
  id: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error' | 'message';
  duration?: number;
  avatarUrl?: string;
  onClick?: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationToastService {
  private readonly toastSubject = new Subject<ToastNotification>();
  public readonly toast$ = this.toastSubject.asObservable();

  /**
   * Affiche une notification toast
   * @param notification Configuration de la notification
   */
  show(notification: Omit<ToastNotification, 'id'>): void {
    const id = `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    this.toastSubject.next({
      ...notification,
      id,
      duration: notification.duration ?? 5000
    });
  }

  /**
   * Notification pour un nouveau message
   */
  showMessage(senderName: string, message: string, avatarUrl?: string, onClick?: () => void): void {
    this.show({
      title: senderName,
      message: this.truncateMessage(message),
      type: 'message',
      avatarUrl,
      onClick,
      duration: 6000
    });

    // Son de notification (optionnel)
    this.playNotificationSound();
  }

  /**
   * Notification succès
   */
  success(message: string, title = 'Succès'): void {
    this.show({ title, message, type: 'success' });
  }

  /**
   * Notification erreur
   */
  error(message: string, title = 'Erreur'): void {
    this.show({ title, message, type: 'error', duration: 7000 });
  }

  /**
   * Notification info
   */
  info(message: string, title = 'Information'): void {
    this.show({ title, message, type: 'info' });
  }

  /**
   * Tronque les messages longs
   */
  private truncateMessage(message: string, maxLength = 80): string {
    if (message.length <= maxLength) return message;
    return message.substring(0, maxLength) + '...';
  }

  /**
   * Joue un son de notification (si autorisé par le navigateur)
   */
  private playNotificationSound(): void {
    try {
      const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBTGH0fPTgjMGHm7A7+OZUA0PVKzn77BdGAg+ltryxmwpBSuAy/DVgjoIFme47OahUxELTqPh8bllHAU2jtfzy3QnBSh+yO/aizsKE2S26eKcTg0QWrHm7aBSEgxKoN/yuGYcBTKJz/PQfS0GI3fE7duRPgoUXLPn6qZUEwpEnt/wumwhBTCFzvPUgjQGHWy/7d6aUQ0PUqrl7q1aFgo7k9jvwmwoBSh9xu/ZhDUHHGm75eacTw8NUKbk7aVQEgxIod7xvmYdBS+Dz/LPgjgHH2++7tqWThENT6Hj7qlZGgs9lNzvxGkpBCl7xO3WiDcIF2m95OKfURAMTqPi76JQEQtDnN7uwXAiBDB/zfHPgTkIHmy77diZUREOTaDi7qdYGgs9ltvvxWopBSl7xfHZhjYIGWi55OGgTxIMTaPj8KNSEgxGn+DwwW8hBDB+zPHQgjgIF2y67t6YVBEPTqLl7qVXGQk7lNnvxGwqByh7xO/YhzYIF2m65N+eURENUKPj8KJSFAxGoN/wvnEjBS9+zfDOgjgIF2u78NuZVBEPT6Lk7aRYGQk6k9nvw20qByh7w+7Xhzc');
      audio.volume = 0.3;
      audio.play().catch(() => {
        // Silencieux si le navigateur bloque l'autoplay
      });
    } catch (ignored) {}
  }
}
