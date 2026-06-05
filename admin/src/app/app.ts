import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterOutlet } from '@angular/router';
import { finalize } from 'rxjs';
import { AdminAuthService } from './core/admin-auth.service';

import { LucideAngularModule, Shield, Mail, Lock, LogIn, RefreshCw, Sun, Moon } from 'lucide-angular';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule, RouterOutlet, LucideAngularModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  readonly authService = inject(AdminAuthService);
  private readonly router = inject(Router);

  readonly Shield = Shield;
  readonly Mail = Mail;
  readonly Lock = Lock;
  readonly LogIn = LogIn;
  readonly RefreshCw = RefreshCw;
  readonly Sun = Sun;
  readonly Moon = Moon;

  isDarkTheme = false;

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('spawnta-admin-theme');
    if (savedTheme === 'dark') {
      this.isDarkTheme = true;
      document.body.classList.add('dark-theme');
    } else {
      this.isDarkTheme = false;
      document.body.classList.remove('dark-theme');
    }
  }

  toggleTheme(): void {
    this.isDarkTheme = !this.isDarkTheme;
    if (this.isDarkTheme) {
      document.body.classList.add('dark-theme');
      localStorage.setItem('spawnta-admin-theme', 'dark');
    } else {
      document.body.classList.remove('dark-theme');
      localStorage.setItem('spawnta-admin-theme', 'light');
    }
  }

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
