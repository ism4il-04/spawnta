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
import { Router } from '@angular/router';

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
    FormsModule
  ],
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
  
  // Custom intro message state
  introMessage = '';

  constructor(
    private activityService: ActivityService,
    private authService: AuthService,
    private chatService: ChatService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activity']?.currentValue) {
      this.pendingParticipants = [];
      this.introMessage = ''; // Reset input field on activity switch
      if (this.isHost) {
        this.loadPendingParticipants();
      }
    }
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
    this.router.navigate(['/chat']);
    this.snackBar.open('Redirection vers la messagerie...', 'OK', { duration: 2000 });
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
