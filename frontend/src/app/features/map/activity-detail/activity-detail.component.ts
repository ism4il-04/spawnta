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
  
  pendingParticipants: ActivityParticipantResponse[] = [];
  loadingPending = false;
  approvingId: number | null = null;
  
  // Custom intro message state
  introMessage = '';
  hostQrCode: string | null = null;
  hostQrImageSrc: string | null = null;
  loadingHostQr = false;
  participation: ParticipationStatus | null = null;

  constructor(
    private activityService: ActivityService,
    private authService: AuthService,
    private chatService: ChatService,
    private attendanceService: AttendanceService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']?.currentValue) {
      this.pendingParticipants = [];
      this.introMessage = '';
      this.participation = null;
      this.hostQrCode = null;
      this.hostQrImageSrc = null;
      if (this.isHost) {
        this.loadPendingParticipants();
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
    const isApproval = this.activity.participationMode === 'APPROVAL';
    const message = isApproval ? this.introMessage.trim() : undefined;

    if (isApproval && !message) {
      this.snackBar.open('✍️ Please enter a message for the host.', 'OK', { duration: 3000 });
      return;
    }

    this.activityService.join(this.activity.id, message).subscribe({
      next: () => {
        const successMsg = isApproval 
          ? '📩 Request sent! The host will review your message.' 
          : '🎉 You have successfully joined the activity!';
        this.snackBar.open(successMsg, 'Great', { duration: 4000 });
        this.activity.participantCount++;
        this.introMessage = '';
      },
      error: (err: any) => {
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
        this.pendingParticipants = this.pendingParticipants.filter(item => item.id !== participant.id);
        this.approvingId = null;
        this.activity.participantCount++;
        this.snackBar.open(`✔️ Approved ${participant.firstName} successfully!`, 'Done', { duration: 3000 });
      },
      error: (err: any) => {
        this.approvingId = null;
        const errMsg = err.error?.error || 'Failed to approve participant.';
        this.snackBar.open(errMsg, 'Close', { duration: 4000 });
      }
    });
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

  openGroupChat() {
    this.router.navigate(['/chat'], { queryParams: { activityId: this.activity.id } });
    this.snackBar.open('Redirection vers la messagerie...', 'OK', { duration: 2000 });
  }

  showHostQrCode(): void {
    this.loadingHostQr = true;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.attendanceService.initiateCheckIn(
          this.activity.id,
          pos.coords.latitude,
          pos.coords.longitude
        ).subscribe({
          next: (res) => {
            this.hostQrCode = res.qrCode;
            this.hostQrImageSrc = qrCodeImageSrc(res.qrCode);
            this.loadingHostQr = false;
          },
          error: (err) => {
            this.loadingHostQr = false;
            this.snackBar.open(err.error?.error || 'Impossible de générer le QR', 'Fermer', { duration: 4000 });
          }
        });
      },
      () => {
        this.loadingHostQr = false;
        this.snackBar.open('Autorisez la géolocalisation pour afficher le QR', 'Fermer', { duration: 3000 });
      }
    );
  }

  contactUser(userId: number) {
    this.chatService.createPrivateChat(userId).subscribe({
      next: (chat) => {
        this.router.navigate(['/chat']);
        this.snackBar.open('Chat privé ouvert', 'OK', { duration: 2000 });
      },
      error: (err) => {
        console.error('Failed to create private chat', err);
        this.snackBar.open('Erreur lors de la création du chat', 'Fermer', { duration: 3000 });
      }
    });
  }
}
