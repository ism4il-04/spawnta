import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { ActivityParticipantResponse, ActivityResponse, ActivityService } from '../../../core/services/activity.service';
import { AuthService } from '../../../core/services/auth.service';
import { ChatService } from '../../../core/services/chat.service';
import { Router, RouterLink } from '@angular/router';
import { ActivityRatingComponent } from '../../activities/activity-rating/activity-rating';
import { AttendanceService, ParticipationStatus } from '../../../core/services/attendance.service';
import { qrCodeImageSrc } from '../../../core/utils/qr-code.util';

@Component({
  selector: 'app-activity-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatTooltipModule,
    FormsModule,
    RouterLink,
    ActivityRatingComponent
  ],
  providers: [DatePipe],
  templateUrl: './activity-detail.component.html',
  styleUrls: ['./activity-detail.component.scss']
})
export class ActivityDetailComponent implements OnChanges {
  @Input() activity!: ActivityResponse;
  @Output() closePanel = new EventEmitter<void>();
  @Output() editActivity = new EventEmitter<ActivityResponse>();
  @Output() deleted = new EventEmitter<number>();
  @Output() activityChanged = new EventEmitter<ActivityResponse>();

  get activityTimeStatus(): { status: string; detail: string; cssClass: string } {
    if (!this.activity || !this.activity.scheduledAt) {
      return { status: 'Flexible', detail: '', cssClass: 'status-flexible' };
    }

    const start = new Date(this.activity.scheduledAt);
    const duration = this.activity.durationMinutes || 0;
    const end = new Date(start.getTime() + duration * 60 * 1000);
    const now = new Date();

    if (duration <= 0) {
      if (now < start) {
        const diffMs = start.getTime() - now.getTime();
        const diffMins = Math.round(diffMs / (60 * 1000));
        const diffHours = Math.round(diffMs / (60 * 60 * 1000));
        const diffDays = Math.round(diffMs / (24 * 60 * 60 * 1000));

        let detail = '';
        if (diffMins < 60) {
          detail = `starts in ${diffMins}m`;
        } else if (diffHours < 24) {
          detail = `starts in ${diffHours}h`;
        } else {
          detail = `starts in ${diffDays}d`;
        }
        return { status: 'Open', detail, cssClass: 'status-open' };
      } else {
        return { status: 'Ongoing', detail: 'started', cssClass: 'status-ongoing' };
      }
    }

    if (now < start) {
      const diffMs = start.getTime() - now.getTime();
      const diffMins = Math.round(diffMs / (60 * 1000));
      const diffHours = Math.round(diffMs / (60 * 60 * 1000));
      const diffDays = Math.round(diffMs / (24 * 60 * 60 * 1000));

      let detail = '';
      if (diffMins < 60) {
        detail = `starts in ${diffMins}m`;
      } else if (diffHours < 24) {
        detail = `starts in ${diffHours}h`;
      } else {
        detail = `starts in ${diffDays}d`;
      }

      return { status: 'Open', detail, cssClass: 'status-open' };
    } else if (now >= start && now <= end) {
      const diffMs = end.getTime() - now.getTime();
      const diffMins = Math.round(diffMs / (60 * 1000));
      const diffHours = Math.floor(diffMs / (60 * 60 * 1000));
      const remainingMins = Math.round((diffMs % (60 * 60 * 1000)) / (60 * 1000));

      let detail = '';
      if (diffMins < 60) {
        detail = `ends in ${diffMins}m`;
      } else {
        detail = `ends in ${diffHours}h ${remainingMins}m`;
      }

      return { status: 'Ongoing', detail, cssClass: 'status-ongoing' };
    } else {
      const diffMs = now.getTime() - end.getTime();
      const diffMins = Math.round(diffMs / (60 * 1000));
      const diffHours = Math.round(diffMs / (60 * 60 * 1000));
      const diffDays = Math.round(diffMs / (24 * 60 * 60 * 1000));

      let detail = '';
      if (diffMins < 60) {
        detail = `ended ${diffMins}m ago`;
      } else if (diffHours < 24) {
        detail = `ended ${diffHours}h ago`;
      } else {
        detail = `ended ${diffDays}d ago`;
      }

      return { status: 'Finished', detail, cssClass: 'status-finished' };
    }
  }

  pendingParticipants: ActivityParticipantResponse[] = [];
  loadingPending = false;
  approvingId: number | null = null;

  pendingAttendances: any[] = [];
  loadingAttendances = false;
  confirmingAttendanceId: number | null = null;

  // Custom intro message state
  introMessage = '';
  joining = false;
  hostQrCode: string | null = null;
  hostQrImageSrc: string | null = null;
  loadingHostQr = false;
  participation: ParticipationStatus | null = null;

  constructor(
    private activityService: ActivityService,
    protected readonly authService: AuthService,
    private chatService: ChatService,
    private attendanceService: AttendanceService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']?.currentValue) {
      this.pendingParticipants = [];
      this.pendingAttendances = [];
      this.introMessage = '';
      this.participation = null;
      this.hostQrCode = null;
      this.hostQrImageSrc = null;
      if (this.isHost) {
        this.loadPendingParticipants();
        this.loadPendingAttendances();
      }
      this.loadParticipationStatus();
    }
  }

  private loadParticipationStatus(): void {
    if (!this.activity?.id) return;
    this.attendanceService.getMyParticipationStatus(this.activity.id).subscribe({
      next: (status) => this.participation = status,
      error: () => this.participation = null
    });
  }

  get isHost(): boolean {
    return this.authService.currentUserValue?.email === this.activity?.hostEmail;
  }

  joinActivity() {
    if (this.joining) return;

    const isApproval = this.activity.participationMode === 'APPROVAL';
    const message = isApproval ? this.introMessage.trim() : undefined;

    if (isApproval && !message) {
      this.snackBar.open('✍️ Please enter a message for the host.', 'OK', { duration: 3000 });
      return;
    }

    this.joining = true;
    this.activityService.joinActivity(this.activity.id, message).subscribe({
      next: () => {
        const successMsg = isApproval
          ? '📩 Request sent! The host will review your message.'
          : '🎉 You have successfully joined the activity!';
        this.snackBar.open(successMsg, 'Great', { duration: 4000 });
        if (isApproval) {
          this.participation = {
            host: false,
            joined: false,
            pendingRequest: true,
            canCheckIn: false,
            canRate: false,
            hasRated: false,
            attendanceStatus: null
          };
        } else {
          this.activity.participantCount++;
          this.participation = {
            host: false,
            joined: true,
            pendingRequest: false,
            canCheckIn: false,
            canRate: false,
            hasRated: false,
            attendanceStatus: null
          };
        }
        this.introMessage = '';
        this.joining = false;
        this.activityChanged.emit(this.activity);
        this.loadParticipationStatus();
      },
      error: (err: any) => {
        this.joining = false;
        console.error('Join failed', err);
        const errMsg = err.error?.error || 'Failed to join. Please try again.';
        this.snackBar.open(errMsg, 'Close', { duration: 4000 });
      }
    });
  }

  approveParticipant(participant: ActivityParticipantResponse) {
    this.approvingId = participant.id;
    this.activityService.approveParticipant(this.activity.id, participant.id).subscribe({
      next: () => {
        this.snackBar.open(`✔️ ${participant.firstName} approved!`, 'Done', { duration: 3000 });
        this.pendingParticipants = this.pendingParticipants.filter(p => p.id !== participant.id);
        this.approvingId = null;
        this.activity.participantCount++;
      },
      error: (err: any) => {
        this.approvingId = null;
        this.snackBar.open(err.error?.error || 'Approval failed', 'Close', { duration: 4000 });
      }
    });
  }

  onEdit() {
    this.editActivity.emit(this.activity);
  }

  onDelete() {
    if (confirm('Are you sure you want to delete this activity? This cannot be undone.')) {
      this.activityService.deleteActivity(this.activity.id).subscribe({
        next: () => {
          this.snackBar.open('🗑️ Activity deleted successfully.', 'OK', { duration: 3000 });
          this.deleted.emit(this.activity.id);
        },
        error: (err: any) => {
          this.snackBar.open(err.error?.error || 'Failed to delete activity.', 'Close', { duration: 4000 });
        }
      });
    }
  }

  private loadPendingParticipants() {
    this.loadingPending = true;
    this.activityService.getPendingParticipants(this.activity.id).subscribe({
      next: (participants) => {
        this.pendingParticipants = participants;
        this.loadingPending = false;
      },
      error: () => {
        this.loadingPending = false;
      }
    });
  }

  private loadPendingAttendances() {
    this.loadingAttendances = true;
    this.attendanceService.getPendingAttendances(this.activity.id).subscribe({
      next: (attendances) => {
        this.pendingAttendances = attendances;
        this.loadingAttendances = false;
      },
      error: () => {
        this.loadingAttendances = false;
      }
    });
  }

  confirmAttendance(attendance: any) {
    this.confirmingAttendanceId = attendance.attendanceId;
    this.attendanceService.hostConfirmAttendance(this.activity.id, [attendance.userId]).subscribe({
      next: () => {
        this.pendingAttendances = this.pendingAttendances.filter(item => item.attendanceId !== attendance.attendanceId);
        this.confirmingAttendanceId = null;
        this.snackBar.open(`✔️ Attendance confirmed for ${attendance.firstName}!`, 'Done', { duration: 3000 });
      },
      error: (err: any) => {
        this.confirmingAttendanceId = null;
        const errMsg = err.error?.error || 'Failed to confirm attendance.';
        this.snackBar.open(errMsg, 'Close', { duration: 4000 });
      }
    });
  }

  openGroupChat() {
    this.router.navigate(['/chat'], { queryParams: { activityId: this.activity.id } });
    this.snackBar.open('Redirecting to group chat...', 'OK', { duration: 2000 });
  }

  showHostQrCode(): void {
    this.loadingHostQr = true;
    this.attendanceService.initiateCheckIn(this.activity.id, 0, 0).subscribe({
      next: (res) => {
        this.hostQrCode = res.qrCode;
        this.hostQrImageSrc = qrCodeImageSrc(res.qrCode);
        this.loadingHostQr = false;
      },
      error: (err) => {
        this.loadingHostQr = false;
        this.snackBar.open(err.error?.error || 'Unable to generate QR code.', 'Close', { duration: 4000 });
      }
    });
  }

  contactUser(userId: number) {
    this.chatService.createPrivateChat(userId).subscribe({
      next: (chat) => {
        this.router.navigate(['/chat']);
        this.snackBar.open('Private chat opened', 'OK', { duration: 2000 });
      },
      error: (err) => {
        console.error('Failed to create private chat', err);
        this.snackBar.open('Error creating chat.', 'Close', { duration: 3000 });
      }
    });
  }
}
