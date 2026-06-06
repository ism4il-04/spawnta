import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface SubscriptionPlanAdmin {
  id: number;
  tier: string;
  name: string;
  description: string | null;
  monthlyPrice: number;
  features: string[];
}

export interface UserSubscriptionAdmin {
  id: number;
  userId: number;
  userEmail: string;
  userName: string;
  tier: string;
  planName: string;
  status: string;
  startDate: string;
  renewalDate: string | null;
  endDate: string | null;
}

export interface AdminTransaction {
  id: number;
  userEmail: string;
  amount: number;
  currency: string;
  status: string;
  timestamp: string;
  stripeId: string | null;
}

export interface AdminSubscriptions {
  totalPlans: number;
  activeSubscriptions: number;
  pendingSubscriptions: number;
  pastDueSubscriptions: number;
  cancelledSubscriptions: number;
  successfulPayments: number;
  monthlyRecurringRevenue: number;
  plans: SubscriptionPlanAdmin[];
  subscriptions: UserSubscriptionAdmin[];
  recentTransactions: AdminTransaction[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminSubscriptionsService {
  private readonly apiUrl = '/api/admin/subscriptions';

  constructor(private http: HttpClient) {}

  getSubscriptions(): Observable<AdminSubscriptions> {
    return this.http.get<AdminSubscriptions>(this.apiUrl);
  }
}
