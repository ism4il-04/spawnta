import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LucideAngularModule, LayoutDashboard, Users, Shield, Calendar, CreditCard, LogOut, ChevronLeft, ChevronRight } from 'lucide-angular';
import { AdminAuthService } from '../../core/admin-auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, LucideAngularModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  readonly authService = inject(AdminAuthService);
  
  readonly LayoutDashboard = LayoutDashboard;
  readonly Users = Users;
  readonly Shield = Shield;
  readonly Calendar = Calendar;
  readonly CreditCard = CreditCard;
  readonly LogOut = LogOut;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;

  toggleSidebar() {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed);
  }

  logout() {
    this.authService.logout();
  }
}
