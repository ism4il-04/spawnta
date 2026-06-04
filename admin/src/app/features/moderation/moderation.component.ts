import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, Observable } from 'rxjs';
import { ActivityReportAdmin, AdminModeration, AdminModerationService, UserReportAdmin } from '../../core/admin-moderation.service';
import { PageHeaderComponent } from '../../shared/components/ui/page-header/page-header.component';
import { LucideAngularModule, Shield, Users, Calendar, AlertTriangle, CheckCircle, XCircle, Search, Filter, Clock, MoreVertical, MessageSquare, ChevronRight, RefreshCw } from 'lucide-angular';

@Component({
  selector: 'app-moderation',
  standalone: true,
  imports: [CommonModule, FormsModule, PageHeaderComponent, LucideAngularModule],
  templateUrl: './moderation.component.html',
  styleUrls: ['./moderation.component.scss']
})
export class ModerationComponent implements OnInit {
  private readonly moderationService = inject(AdminModerationService);
  private readonly cd = inject(ChangeDetectorRef);

  data: AdminModeration | null = null;
  loading = false;
  actionId = '';
  errorMessage = '';
  status = 'all';
  searchQuery = '';
  selectedReport: UserReportAdmin | ActivityReportAdmin | null = null;
  selectedKind: 'user' | 'activity' | null = null;
  activeTab: 'all' | 'users' | 'activities' = 'all';

  readonly Shield = Shield;
  readonly Users = Users;
  readonly Calendar = Calendar;
  readonly AlertTriangle = AlertTriangle;
  readonly CheckCircle = CheckCircle;
  readonly XCircle = XCircle;
  readonly Search = Search;
  readonly Filter = Filter;
  readonly Clock = Clock;
  readonly MoreVertical = MoreVertical;
  readonly MessageSquare = MessageSquare;
  readonly ChevronRight = ChevronRight;
  readonly RefreshCw = RefreshCw;

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.errorMessage = '';
    this.moderationService.getReports(this.status).pipe(
      finalize(() => {
        this.loading = false;
        this.cd.detectChanges();
      })
    ).subscribe({
      next: data => {
        this.data = data;
        if (!this.selectedReport) {
          this.selectedReport = data.userReports[0] ?? data.activityReports[0] ?? null;
          this.selectedKind = data.userReports[0] ? 'user' : data.activityReports[0] ? 'activity' : null;
        }
        this.cd.detectChanges();
      },
      error: error => {
        this.errorMessage = error?.error?.error ?? 'Impossible de charger la moderation.';
        this.cd.detectChanges();
      }
    });
  }

  updateReport(report: UserReportAdmin | ActivityReportAdmin, action: 'investigate' | 'resolve' | 'dismiss'): void {
    const notes = window.prompt('Moderation notes', action === 'dismiss' ? 'Report dismissed' : 'Admin decision');
    if (notes === null) return;
    
    this.actionId = `${this.selectedKind}-${report.id}`;
    
    let obs: Observable<any>;
    if (this.selectedKind === 'user') {
      obs = this.moderationService.updateUserReport(report.id, action, notes);
    } else {
      obs = this.moderationService.updateActivityReport(report.id, action, notes);
    }

    obs.pipe(
      finalize(() => {
        this.actionId = '';
        this.cd.detectChanges();
      })
    ).subscribe({
      next: () => this.loadReports(),
      error: (error: any) => {
        this.errorMessage = error?.error?.error ?? 'Action failed.';
        this.cd.detectChanges();
      }
    });
  }

  selectUserReport(report: UserReportAdmin): void {
    this.selectedReport = report;
    this.selectedKind = 'user';
  }

  selectActivityReport(report: ActivityReportAdmin): void {
    this.selectedReport = report;
    this.selectedKind = 'activity';
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  get filteredUserReports(): UserReportAdmin[] {
    if (!this.data) return [];
    return this.data.userReports.filter(r => 
      r.reportedUserEmail.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      r.reporterEmail.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  get filteredActivityReports(): ActivityReportAdmin[] {
    if (!this.data) return [];
    return this.data.activityReports.filter(r => 
      r.activityTitle.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      r.reporterEmail.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  asUserReport(report: any): UserReportAdmin {
    return report as UserReportAdmin;
  }

  asActivityReport(report: any): ActivityReportAdmin {
    return report as ActivityReportAdmin;
  }
}
