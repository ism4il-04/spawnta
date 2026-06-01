import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

import { RecommendationsFeedComponent } from '../recommendations/recommendations-feed/recommendations-feed';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, RecommendationsFeedComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  protected readonly authService = inject(AuthService);

  protected readonly features = [
    {
      title: 'Carte interactive',
      description: 'Explorer les activites autour de vous, rechercher une ville et ouvrir les details de chaque sortie.',
      cta: 'Ouvrir la carte',
      link: '/map'
    },
    {
      title: 'Mon profil',
      description: 'Mettre a jour votre bio, vos reseaux, vos interets, votre galerie et votre avatar.',
      cta: 'Gerer mon profil',
      link: '/profile'
    },
    {
      title: 'Authentification',
      description: 'Creer un compte, verifier votre email et vous connecter pour acceder aux fonctionnalites protegees.',
      cta: 'Creer un compte',
      link: '/signup'
    }
  ];
}
