import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {
  ActivityParticipantResponse,
  ActivityResponse,
  ActivityService
} from '../../../core/services/activity.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-activity-detail',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  providers: [DatePipe],
  templateUrl: './activity-detail.component.html',
  styleUrl: './activity-detail.component.scss'
})
export class ActivityDetailComponent implements OnChanges {
  @Input() activity!: ActivityResponse;
  @Output() closePanel = new EventEmitter<void>();
  pendingParticipants: ActivityParticipantResponse[] = [];
  loadingPending = false;
  approvingId: number | null = null;

  constructor(
    private activityService: ActivityService,
    private authService: AuthService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']?.currentValue) {
      this.pendingParticipants = [];
      if (this.isHost) {
        this.loadPendingParticipants();
      }
    }
  }

  get isHost(): boolean {
    return this.authService.currentUserValue?.email === this.activity?.hostEmail;
  }

  joinActivity() {
    const introMessage = this.activity.participationMode === 'APPROVAL'
      ? window.prompt('Add a short introduction for the host (max 150 characters):', '')?.trim()
      : undefined;

    if (this.activity.participationMode === 'APPROVAL' && !introMessage) {
      alert('An introduction message is required for approval-based activities.');
      return;
    }

    this.activityService.join(this.activity.id, introMessage).subscribe({
      next: () => {
        alert(this.activity.participationMode === 'DIRECT' ? 'Joined successfully!' : 'Join request sent!');
      },
      error: (err: any) => {
        console.error('Join failed', err);
        alert(err.error?.error || 'Failed to join');
      }
    });
  }

  approveParticipant(participant: ActivityParticipantResponse) {
    this.approvingId = participant.id;
    this.activityService.approveParticipant(this.activity.id, participant.id).subscribe({
      next: () => {
        this.pendingParticipants = this.pendingParticipants.filter(item => item.id !== participant.id);
        this.approvingId = null;
        alert('Participant approved.');
      },
      error: (err: any) => {
        this.approvingId = null;
        alert(err.error?.error || 'Failed to approve participant');
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
}
