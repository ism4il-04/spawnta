import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminAuthService } from '../../core/admin-auth.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent {
  readonly authService = inject(AdminAuthService);
}
