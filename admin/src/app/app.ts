import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { finalize } from 'rxjs';
import { AdminAuthService } from './core/admin-auth.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  readonly authService = inject(AdminAuthService);
  private readonly router = inject(Router);

  email = 'admin@spawnta.com';
  password = 'demo1234';
  loading = false;
  errorMessage = '';

  login(): void {
    this.loading = true;
    this.errorMessage = '';
    this.authService.login(this.email.trim(), this.password).pipe(
      finalize(() => this.loading = false)
    ).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: error => {
        this.errorMessage = error?.error?.error ?? error?.message ?? 'Connexion admin impossible.';
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/dashboard');
  }
}
