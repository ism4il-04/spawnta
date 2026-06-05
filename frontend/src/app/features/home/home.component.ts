import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

import { RecommendationsFeedComponent } from '../recommendations/recommendations-feed/recommendations-feed';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, RecommendationsFeedComponent, MatIconModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  protected readonly authService = inject(AuthService);

  protected get filteredFeatures() {
    return this.features.filter(f => {
      if (f.id === 'auth' && this.authService.isLoggedIn()) {
        return false;
      }
      return true;
    });
  }

  private readonly features = [
    {
      id: 'carte',
      icon: 'map',
      title: 'Interactive Map',
      description: 'Explore activities around you, search for cities, and check details of each outing in one click.',
      cta: 'Open the Map',
      link: '/map'
    },
    {
      id: 'profil',
      icon: 'person',
      title: 'My Profile',
      description: 'Update your bio, social networks, interests, gallery, and custom avatars to showcase your activities.',
      cta: 'Manage Profile',
      link: '/profile'
    },
    {
      id: 'auth',
      icon: 'lock',
      title: 'Secure Account',
      description: 'Create an account, verify your email, and access all protected features of the community platform.',
      cta: 'Sign Up Now',
      link: '/signup'
    },
    {
      id: 'subscription',
      icon: 'workspace_premium',
      title: 'Premium Subscription',
      description: 'Compare available plans and initiate Stripe checkout from your account to unlock premium perks.',
      cta: 'View Plans',
      link: '/subscription'
    }
  ];
}
