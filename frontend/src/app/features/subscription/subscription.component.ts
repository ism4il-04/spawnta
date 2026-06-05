import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { finalize, Subscription as RxSubscription, interval, take, switchMap, filter, of, Subject, takeUntil } from 'rxjs';
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
export class SubscriptionComponent implements OnInit, OnDestroy {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroy$ = new Subject<void>();

  plans: SubscriptionPlan[] = [];
  currentSubscription: UserSubscription | null = null;
  loadingPlans = true;
  loadingCurrent = false;
  upgradeTier: string | null = null;
  plansError = false;
  pollingSubscription: RxSubscription | null = null;
  isProcessingPayment = false;

  ngOnInit(): void {
    this.loadPlans();
    this.loadCurrentSubscription();
    this.checkCheckoutStatus();
  }

  ngOnDestroy(): void {
    this.stopPolling();
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkCheckoutStatus(): void {
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['checkout'] === 'success') {
        this.isProcessingPayment = true;
        this.snackBar.open('Paiement reussi ! Finalisation de votre abonnement...', 'Fermer', { duration: 5000 });
        this.startPollingSubscription();
      } else if (params['checkout'] === 'cancel') {
        this.snackBar.open('Paiement annule.', 'Fermer', { duration: 4000 });
      }
    });
  }

  private startPollingSubscription(): void {
    const initialTier = this.currentTier;
    this.stopPolling();
    this.isProcessingPayment = true;

    // Fast polling: every 2 seconds for a smoother "real-time" feel
    this.pollingSubscription = interval(2000).pipe(
      take(15), // Up to 30 seconds
      switchMap(() => this.subscriptionService.getCurrentSubscription()),
      filter(sub => {
        const newTier = (sub?.plan?.tier ?? 'FREE').toUpperCase();
        return newTier !== initialTier;
      })
    ).subscribe({
      next: subscription => {
        this.currentSubscription = subscription;
        this.isProcessingPayment = false;
        this.snackBar.open('🚀 Félicitations ! Votre abonnement est maintenant actif.', 'Super !', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
        this.stopPolling();
        // Remove query params without refreshing
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { checkout: null },
          queryParamsHandling: 'merge',
          replaceUrl: true
        });
      },
      complete: () => {
        if (this.isProcessingPayment) {
          this.isProcessingPayment = false;
          this.snackBar.open('La validation prend un peu de temps, mais votre paiement est bien reçu !', 'OK', { duration: 6000 });
        }
      }
    });
  }

  private stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get currentTier(): string {
    return (this.currentSubscription?.plan?.tier ?? 'FREE').toUpperCase();
  }

  loadPlans(): void {
    this.loadingPlans = true;
    this.subscriptionService.getPlans().pipe(
      finalize(() => {
        this.loadingPlans = false;
        console.log('Plans loaded:', this.plans);
      })
    ).subscribe({
      next: (plans) => {
        this.plans = this.sortPlans(plans);
        this.plansError = false;
      },
      error: (error) => {
        console.error('Error loading plans:', error);
        this.plansError = true;
        this.snackBar.open('Erreur lors du chargement des plans.', 'Réessayer', { duration: 5000 })
          .onAction().subscribe(() => this.loadPlans());
      }
    });
  }

  loadCurrentSubscription(): void {
    if (!this.authService.isLoggedIn()) {
      return;
    }

    this.loadingCurrent = true;
    this.subscriptionService.getCurrentSubscription().pipe(
      finalize(() => {
        this.loadingCurrent = false;
      })
    ).subscribe({
      next: subscription => {
        this.currentSubscription = subscription;
      },
      error: error => {
        if (error.status !== 404) {
          this.snackBar.open('Abonnement actuel indisponible.', 'Fermer', { duration: 4000 });
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
      finalize(() => {
        this.upgradeTier = null;
      })
    ).subscribe({
      next: response => {
        if (response.checkoutUrl) {
          window.location.href = response.checkoutUrl;
          return;
        }
        this.snackBar.open('Session Stripe créée, mais URL de paiement manquante.', 'Fermer', { duration: 4000 });
      },
      error: error => {
        const message = error?.error?.error ?? 'Impossible d\'initier le paiement.';
        this.snackBar.open(message, 'Fermer', { duration: 5000 });
      }
    });
  }

  isCurrentPlan(plan: SubscriptionPlan): boolean {
    return plan.tier.toUpperCase() === this.currentTier;
  }

  actionLabel(plan: SubscriptionPlan): string {
    if (this.isCurrentPlan(plan)) {
      return 'Plan actuel';
    }
    if (plan.tier === 'FREE') {
      return 'Inclus';
    }
    if (!this.authService.isLoggedIn()) {
      return 'Se connecter';
    }
    return this.upgradeTier === plan.tier ? 'Préparation...' : 'Choisir ce plan';
  }

  monthlyPrice(plan: SubscriptionPlan): string {
    return Number(plan.monthlyPrice || 0).toFixed(2);
  }

  private sortPlans(plans: SubscriptionPlan[]): SubscriptionPlan[] {
    const order = new Map([
      ['free', 0],
      ['starter', 1],
      ['pro', 2],
      ['FREE', 0],
      ['STARTER', 1],
      ['PROFESSIONAL', 2]
    ]);
    return [...plans].sort((a, b) => (order.get(a.tier) ?? 99) - (order.get(b.tier) ?? 99));
  }
}
