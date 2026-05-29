import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  protected readonly authService = inject(AuthService);

  protected readonly features = [
    {
      id: 'carte',
      icon: 'map',
      title: 'Carte interactive',
      description: 'Explorez les activités autour de vous, recherchez une ville et consultez les détails de chaque sortie en un clic.',
      cta: 'Ouvrir la carte',
      link: '/map'
    },
    {
      id: 'profil',
      icon: 'person',
      title: 'Mon profil',
      description: 'Mettez à jour votre bio, vos réseaux sociaux, vos centres d\'intérêt, votre galerie et votre avatar.',
      cta: 'Gérer mon profil',
      link: '/profile'
    },
    {
      id: 'auth',
      icon: 'lock',
      title: 'Compte sécurisé',
      description: 'Créez un compte, vérifiez votre email et accédez à toutes les fonctionnalités protégées de la plateforme.',
      cta: 'Créer un compte',
      link: '/signup'
    }
  ];
}
