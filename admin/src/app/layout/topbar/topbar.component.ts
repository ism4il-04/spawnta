import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Sun, Moon } from 'lucide-angular';
import { AdminAuthService } from '../../core/admin-auth.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent {
  readonly authService = inject(AdminAuthService);
  readonly Sun = Sun;
  readonly Moon = Moon;

  isDarkTheme = false;

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

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('spawnta-admin-theme');
    this.isDarkTheme = savedTheme === 'dark' || document.body.classList.contains('dark-theme');
    document.body.classList.toggle('dark-theme', this.isDarkTheme);
  }
}
