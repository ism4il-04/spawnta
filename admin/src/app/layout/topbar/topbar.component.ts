import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Search, Bell, User, Settings, HelpCircle } from 'lucide-angular';
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
  
  readonly Search = Search;
  readonly Bell = Bell;
  readonly User = User;
  readonly Settings = Settings;
  readonly HelpCircle = HelpCircle;

  notificationCount = 3; // Mock value
}
