import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AdminUser, AdminUsersResponse, AdminUsersService } from '../../core/admin-users.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  private readonly usersService = inject(AdminUsersService);
  private readonly cd = inject(ChangeDetectorRef);

  users: AdminUser[] = [];
  summary: AdminUsersResponse['summary'] | null = null;
  loading = false;
  actionUserId: number | null = null;
  errorMessage = '';

  search = '';
  status = 'all';
  tier = 'all';

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.usersService.getUsers({
      search: this.search.trim(),
      status: this.status,
      tier: this.tier
    }).pipe(
      finalize(() => {
        this.loading = false;
        this.cd.detectChanges();
      })
    ).subscribe({
      next: response => {
        this.users = response.users;
        this.summary = response.summary;
        this.cd.detectChanges();
      },
      error: error => {
        this.errorMessage = error?.error?.error ?? 'Impossible de charger les utilisateurs.';
        this.cd.detectChanges();
      }
    });
  }

  ban(user: AdminUser): void {
    const reason = window.prompt(`Raison du ban pour ${user.email}`, 'Violation des regles');
    if (reason === null) return;
    this.actionUserId = user.id;
    this.usersService.banUser(user.id, reason).pipe(
      finalize(() => this.actionUserId = null)
    ).subscribe({
      next: updated => this.replaceUser(updated),
      error: error => this.errorMessage = error?.error?.error ?? 'Ban impossible.'
    });
  }

  suspend(user: AdminUser): void {
    const daysValue = window.prompt(`Nombre de jours de suspension pour ${user.email}`, '7');
    if (daysValue === null) return;
    const days = Number(daysValue) || 7;
    const reason = window.prompt('Raison de la suspension', 'Verification moderation');
    if (reason === null) return;
    this.actionUserId = user.id;
    this.usersService.suspendUser(user.id, reason, days).pipe(
      finalize(() => this.actionUserId = null)
    ).subscribe({
      next: updated => this.replaceUser(updated),
      error: error => this.errorMessage = error?.error?.error ?? 'Suspension impossible.'
    });
  }

  restore(user: AdminUser): void {
    this.actionUserId = user.id;
    this.usersService.restoreUser(user.id).pipe(
      finalize(() => this.actionUserId = null)
    ).subscribe({
      next: updated => this.replaceUser(updated),
      error: error => this.errorMessage = error?.error?.error ?? 'Restauration impossible.'
    });
  }

  changeRole(user: AdminUser, newRole: string): void {
    if (!window.confirm(`Changer le role de ${user.email} en ${newRole} ?`)) return;
    this.actionUserId = user.id;
    this.usersService.updateRole(user.id, newRole).pipe(
      finalize(() => this.actionUserId = null)
    ).subscribe({
      next: updated => this.replaceUser(updated),
      error: error => this.errorMessage = error?.error?.error ?? 'Changement de role impossible.'
    });
  }

  statusLabel(user: AdminUser): string {
    if (user.banned) return 'Banni';
    if (this.isSuspended(user)) return 'Suspendu';
    if (!user.emailVerified) return 'Non verifie';
    return 'Actif';
  }

  statusClass(user: AdminUser): string {
    if (user.banned) return 'danger';
    if (this.isSuspended(user)) return 'warning';
    if (!user.emailVerified) return 'muted';
    return 'success';
  }

  roleClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'role-admin';
      case 'MODERATOR': return 'role-mod';
      default: return 'role-user';
    }
  }

  isSuspended(user: AdminUser): boolean {
    return !!user.suspendedUntil && new Date(user.suspendedUntil).getTime() > Date.now();
  }

  trackById(_: number, user: AdminUser): number {
    return user.id;
  }

  private replaceUser(updated: AdminUser): void {
    this.users = this.users.map(user => user.id === updated.id ? updated : user);
    this.loadUsers();
  }
}
