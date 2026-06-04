import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { finalize } from 'rxjs';
import { AdminSubscriptions, AdminSubscriptionsService } from '../../core/admin-subscriptions.service';

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './subscriptions.component.html',
  styleUrls: ['./subscriptions.component.scss']
})
export class SubscriptionsComponent implements OnInit {
  private readonly subscriptionsService = inject(AdminSubscriptionsService);
  private readonly cd = inject(ChangeDetectorRef);

  data: AdminSubscriptions | null = null;
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadSubscriptions();
  }

  loadSubscriptions(): void {
    this.loading = true;
    this.errorMessage = '';
    this.subscriptionsService.getSubscriptions().pipe(
      finalize(() => {
        this.loading = false;
        this.cd.detectChanges();
      })
    ).subscribe({
      next: data => {
        this.data = data;
        this.cd.detectChanges();
      },
      error: error => {
        this.errorMessage = error?.error?.error ?? 'Impossible de charger les abonnements.';
        this.cd.detectChanges();
      }
    });
  }

  estimatedMrr(): number {
    if (!this.data) return 0;
    return this.data.subscriptions
      .filter(subscription => subscription.status === 'ACTIVE')
      .reduce((total, subscription) => {
        const plan = this.data?.plans.find(item => item.tier === subscription.tier);
        return total + (plan?.monthlyPrice ?? 0);
      }, 0);
  }

  churnRate(): number {
    if (!this.data) return 0;
    const total = this.data.activeSubscriptions + this.data.cancelledSubscriptions + this.data.pastDueSubscriptions;
    return total === 0 ? 0 : (this.data.cancelledSubscriptions / total) * 100;
  }

  statusClass(status: string): string {
    return status.toLowerCase().replace('_', '-');
  }
}
