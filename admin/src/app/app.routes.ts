import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ActivitiesComponent } from './features/activities/activities.component';
import { ModerationComponent } from './features/moderation/moderation.component';
import { SubscriptionsComponent } from './features/subscriptions/subscriptions.component';
import { UsersComponent } from './features/users/users.component';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'activities', component: ActivitiesComponent },
      { path: 'users', component: UsersComponent },
      { path: 'subscriptions', component: SubscriptionsComponent },
      { path: 'moderation', component: ModerationComponent }
    ]
  }
];
