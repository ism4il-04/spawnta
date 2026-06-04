import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import {
  SubscriptionPlan,
  SubscriptionService,
  UserSubscription
} from '../../core/services/subscription.service';

@Component({
  selector: 'app-subscription',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule, MatSnackBarModule],
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.scss']
})
export class SubscriptionComponent implements OnInit {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  plans: SubscriptionPlan[] = [];
  currentSubscription: UserSubscription | null = null;
  loadingPlans = true;
  loadingCurrent = false;
  upgradeTier: string | null = null;
  plansError = false;

  ngOnInit(): void {
    this.loadPlans();
    this.loadCurrentSubscription();
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get currentTier(): string {
    return this.currentSubscription?.plan?.tier ?? 'FREE';
  }

  loadPlans(): void {
    this.loadingPlans = true;
    this.plansError = false;

    this.subscriptionService.getPlans().pipe(
      finalize(() => this.loadingPlans = false)
    ).subscribe({
      next: plans => this.plans = this.sortPlans(plans),
      error: () => {
        this.plansError = true;
        this.snackBar.open('Unable to load plans.', 'Close', { duration: 4000 });
      }
    });
  }

  loadCurrentSubscription(): void {
    if (!this.authService.isLoggedIn()) {
      return;
    }

    this.loadingCurrent = true;
    this.subscriptionService.getCurrentSubscription().pipe(
      finalize(() => this.loadingCurrent = false)
    ).subscribe({
      next: subscription => this.currentSubscription = subscription,
      error: error => {
        if (error.status !== 404) {
          this.snackBar.open('Current subscription unavailable.', 'Close', { duration: 4000 });
        }
      }
    });
  }

  selectPlan(plan: SubscriptionPlan): void {
    if (plan.tier === 'FREE' || this.isCurrentPlan(plan)) {
      return;
    }

    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.upgradeTier = plan.tier;
    this.subscriptionService.upgradeSubscription(plan.tier).pipe(
      finalize(() => this.upgradeTier = null)
    ).subscribe({
      next: response => {
        if (response.checkoutUrl) {
          window.location.href = response.checkoutUrl;
          return;
        }
        this.snackBar.open('Stripe session created, but checkout URL is missing.', 'Close', { duration: 4000 });
      },
      error: error => {
        const message = error?.error?.error ?? 'Unable to initiate payment.';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }

  isCurrentPlan(plan: SubscriptionPlan): boolean {
    return plan.tier === this.currentTier;
  }

  actionLabel(plan: SubscriptionPlan): string {
    if (this.isCurrentPlan(plan)) {
      return 'Current plan';
    }
    if (plan.tier === 'FREE') {
      return 'Included';
    }
    if (!this.authService.isLoggedIn()) {
      return 'Sign in';
    }
    return this.upgradeTier === plan.tier ? 'Preparing...' : 'Choose this plan';
  }

  monthlyPrice(plan: SubscriptionPlan): string {
    return Number(plan.monthlyPrice || 0).toFixed(2);
  }

  private sortPlans(plans: SubscriptionPlan[]): SubscriptionPlan[] {
    const order = new Map([
      ['FREE', 0],
      ['STARTER', 1],
      ['PROFESSIONAL', 2]
    ]);
    return [...plans].sort((a, b) => (order.get(a.tier) ?? 99) - (order.get(b.tier) ?? 99));
  }
}
