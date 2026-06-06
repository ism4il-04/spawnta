import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import {
  ActivityService,
  ActivityParticipantResponse,
  MyActivityResponse
} from '../../../core/services/activity.service';
import { AttendanceService } from '../../../core/services/attendance.service';
import { qrCodeImageSrc } from '../../../core/utils/qr-code.util';

@Component({
  selector: 'app-my-activities',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    RouterLink
  ],
  providers: [DatePipe],
  templateUrl: './my-activities.component.html',
  styleUrls: ['./my-activities.component.scss']
})
export class MyActivitiesComponent implements OnInit {
  loading = true;
  hostedActivities: MyActivityResponse[] = [];
  joinedActivities: MyActivityResponse[] = [];

  // Expandable detail panels per activity
  expandedActivityId: number | null = null;

  // Pending check-ins per activity (lazy loaded)
  pendingCheckInsMap: Record<number, any[]> = {};
  loadingCheckIns: Record<number, boolean> = {};

  // Pending join requests per activity
  pendingJoinMap: Record<number, ActivityParticipantResponse[]> = {};
  loadingJoins: Record<number, boolean> = {};

  // Host QR codes per activity
  hostQrImageMap: Record<number, string> = {};
  loadingQrMap: Record<number, boolean> = {};

  // Action states
  approvingJoinId: number | null = null;
  confirmingCheckInId: number | null = null;

  constructor(
    private activityService: ActivityService,
    private attendanceService: AttendanceService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadActivities();
  }

  loadActivities(): void {
    this.loading = true;
    this.activityService.getMyActivities().subscribe({
      next: (activities) => {
        this.hostedActivities = activities.filter(a => a.participation.host);
        this.joinedActivities = activities.filter(a => !a.participation.host);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load activities', 'Close', { duration: 4000 });
      }
    });
  }

  toggleExpand(activityId: number): void {
    if (this.expandedActivityId === activityId) {
      this.expandedActivityId = null;
      return;
    }
    this.expandedActivityId = activityId;
    this.loadPendingCheckInsFor(activityId);
    this.loadPendingJoinsFor(activityId);
  }

  private loadPendingCheckInsFor(activityId: number): void {
    if (this.pendingCheckInsMap[activityId]) return;
    this.loadingCheckIns[activityId] = true;
    this.attendanceService.getPendingAttendances(activityId).subscribe({
      next: (data) => {
        this.pendingCheckInsMap[activityId] = data;
        this.loadingCheckIns[activityId] = false;
      },
      error: () => {
        this.loadingCheckIns[activityId] = false;
      }
    });
  }

  private loadPendingJoinsFor(activityId: number): void {
    if (this.pendingJoinMap[activityId]) return;
    this.loadingJoins[activityId] = true;
    this.activityService.getPendingParticipants(activityId).subscribe({
      next: (data) => {
        this.pendingJoinMap[activityId] = data;
        this.loadingJoins[activityId] = false;
      },
      error: () => {
        this.loadingJoins[activityId] = false;
      }
    });
  }

  approveJoin(activityId: number, participant: ActivityParticipantResponse): void {
    this.approvingJoinId = participant.id;
    this.activityService.approveParticipant(activityId, participant.id).subscribe({
      next: () => {
        this.pendingJoinMap[activityId] = (this.pendingJoinMap[activityId] || [])
          .filter(p => p.id !== participant.id);
        this.approvingJoinId = null;
        this.snackBar.open(`${participant.firstName} approved!`, 'Done', { duration: 3000 });

        // Refresh activity to update participant count
        this.loadActivities();
      },
      error: (err: any) => {
        this.approvingJoinId = null;
        this.snackBar.open(err.error?.error || 'Approval failed', 'Close', { duration: 4000 });
      }
    });
  }

  confirmCheckIn(activityId: number, attendance: any): void {
    this.confirmingCheckInId = attendance.attendanceId;
    this.attendanceService.hostConfirmAttendance(activityId, [attendance.userId]).subscribe({
      next: () => {
        this.pendingCheckInsMap[activityId] = (this.pendingCheckInsMap[activityId] || [])
          .filter(a => a.attendanceId !== attendance.attendanceId);
        this.confirmingCheckInId = null;
        this.snackBar.open(`Presence confirmed for ${attendance.firstName}!`, 'Done', { duration: 3000 });

      },
      error: (err: any) => {
        this.confirmingCheckInId = null;
        this.snackBar.open(err.error?.error || 'Confirmation failed', 'Close', { duration: 4000 });
      }
    });
  }

  showHostQrCode(activityId: number): void {
    if (this.hostQrImageMap[activityId] || this.loadingQrMap[activityId]) return;

    this.loadingQrMap[activityId] = true;
    this.attendanceService.initiateCheckIn(activityId, 0, 0).subscribe({
      next: (res) => {
        const qrImage = qrCodeImageSrc(res.qrCode);
        if (!qrImage) {
          this.snackBar.open('Unable to generate QR code.', 'Close', { duration: 4000 });
          this.loadingQrMap[activityId] = false;
          return;
        }
        this.hostQrImageMap[activityId] = qrImage;
        this.loadingQrMap[activityId] = false;
      },
      error: (err: any) => {
        this.loadingQrMap[activityId] = false;
        this.snackBar.open(err.error?.error || 'Unable to generate QR code.', 'Close', { duration: 4000 });
      }
    });
  }

  getParticipantText(item: MyActivityResponse): string {
    const count = item.activity.participantCount || 0;
    const max = item.activity.maxParticipants;
    return max ? `${count} / ${max} registered` : `${count} registered`;
  }

  getTimeStatus(item: MyActivityResponse): { label: string; cssClass: string } {
    const act = item.activity;
    if (!act.scheduledAt) return { label: 'Flexible', cssClass: 'badge-flexible' };

    const start = new Date(act.scheduledAt);
    const duration = act.durationMinutes || 0;
    const end = new Date(start.getTime() + duration * 60 * 1000);
    const now = new Date();

    if (duration <= 0) {
      return now < start
        ? { label: 'Upcoming', cssClass: 'badge-upcoming' }
        : { label: 'Ongoing', cssClass: 'badge-ongoing' };
    }
    if (now < start) return { label: 'Upcoming', cssClass: 'badge-upcoming' };
    if (now <= end) return { label: 'Ongoing', cssClass: 'badge-ongoing' };
    return { label: 'Finished', cssClass: 'badge-finished' };
  }

  getAttendanceBadge(item: MyActivityResponse): { label: string; cssClass: string } | null {
    const status = item.participation.attendanceStatus;
    if (!status) return null;
    if (status === 'CONFIRMED') return { label: 'Confirmed', cssClass: 'att-confirmed' };
    if (status === 'PENDING') return { label: 'Awaiting Confirmation', cssClass: 'att-pending' };
    return null;
  }
}
