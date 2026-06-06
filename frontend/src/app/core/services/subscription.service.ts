import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface SubscriptionPlan {
  id: number;
  tier: 'FREE' | 'STARTER' | 'PROFESSIONAL' | string;
  name: string;
  description: string;
  monthlyPrice: number;
  discountedPrice?: number;
  discountReason?: string;
  features: string[];
}

export interface UserSubscription {
  id: number;
  plan: SubscriptionPlan;
  status: string;
  startDate: string | null;
  endDate: string | null;
  renewalDate: string | null;
  active: boolean;
}

export interface CheckoutSessionResponse {
  sessionId: string;
  checkoutUrl: string;
  publishableKey: string;
}

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly apiUrl = '/api/subscription';

  constructor(private http: HttpClient) { }

  getPlans(): Observable<SubscriptionPlan[]> {
    return this.http.get<SubscriptionPlan[]>(`${this.apiUrl}/plans`);
  }

  getCurrentSubscription(): Observable<UserSubscription> {
    return this.http.get<UserSubscription>(`${this.apiUrl}/current`);
  }

  upgradeSubscription(tier: string): Observable<CheckoutSessionResponse> {
    const origin = window.location.origin;
    return this.http.post<CheckoutSessionResponse>(`${this.apiUrl}/upgrade`, {
      tier,
      successUrl: `${origin}/subscription?checkout=success`,
      cancelUrl: `${origin}/subscription?checkout=cancel`
    });
  }

  cancelSubscription(reason: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/cancel`, { reason });
  }
}
