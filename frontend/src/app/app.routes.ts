import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { SignupComponent } from './features/auth/signup/signup.component';
import { SignupSuccessComponent } from './features/auth/signup-success/signup-success.component';
import { VerifyEmailComponent } from './features/auth/verify-email/verify-email.component';
import { HomeComponent } from './features/home/home.component';
import { ProfileComponent } from './features/profile/profile.component';
import { MapComponent } from './features/map/map.component';
import { ChatComponent } from './features/chat/chat.component';
import { CheckInComponent } from './features/activities/check-in/check-in';
import { SubscriptionComponent } from './features/subscription/subscription.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '',               component: HomeComponent },
  { path: 'login',          component: LoginComponent },
  { path: 'signup',         component: SignupComponent },
  { path: 'signup-success', component: SignupSuccessComponent },
  { path: 'verify-email',   component: VerifyEmailComponent },
  { path: 'profile',        component: ProfileComponent, canActivate: [authGuard] },
  { path: 'map',            component: MapComponent, canActivate: [authGuard] },
  { path: 'chat',           component: ChatComponent, canActivate: [authGuard] },
  { path: 'subscription',   component: SubscriptionComponent },
  { path: 'check-in/:id',   component: CheckInComponent, canActivate: [authGuard] }
];
