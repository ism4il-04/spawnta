import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ModerationComponent } from './features/moderation/moderation.component';
import { SubscriptionsComponent } from './features/subscriptions/subscriptions.component';
import { UsersComponent } from './features/users/users.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'users', component: UsersComponent },
  { path: 'subscriptions', component: SubscriptionsComponent },
  { path: 'moderation', component: ModerationComponent }
];
