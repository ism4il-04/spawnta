import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AttendanceService, ParticipationStatus } from '../../../core/services/attendance.service';
import { qrCodeImageSrc } from '../../../core/utils/qr-code.util';
import { timeout } from 'rxjs/operators';

@Component({
  selector: 'app-check-in',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './check-in.html',
  styleUrl: './check-in.scss'
})
export class CheckInComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private attendanceService = inject(AttendanceService);
  private snackBar = inject(MatSnackBar);

  activityId!: number;
  loading = false;
  success = false;
  blocked = false;
  blockMessage = '';
  qrCode: string | null = null;
  qrImageSrc: string | null = null;
  activityName = '';
  checkInDeadline = '';
  attendanceStatus: string | null = null;
  private lastLatitude = 0;
  private lastLongitude = 0;

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.activityId = Number(params.get('id'));
      if (!this.activityId) {
        this.router.navigate(['/map']);
        return;
      }
      this.verifyEligibility();
    });
  }

  private verifyEligibility(): void {
    this.loading = true;
    this.attendanceService.getMyParticipationStatus(this.activityId)
      .pipe(timeout(10000))
      .subscribe({
        next: (status: ParticipationStatus) => {
          this.loading = false;
          this.attendanceStatus = status.attendanceStatus;

          if (status.host) {
            this.blocked = true;
            this.blockMessage = 'En tant qu\'hôte, affichez le QR depuis la fiche activité sur la carte.';
            return;
          }

          // Already checked in — show the appropriate status
          if (status.attendanceStatus === 'PENDING' || status.attendanceStatus === 'CONFIRMED') {
            this.blocked = true;
            return;
          }

          if (!status.canCheckIn) {
            this.blocked = true;
            this.blockMessage = status.joined
              ? 'Check-in indisponible pour le moment (hors de la fenêtre horaire de l\'activité).'
              : 'Vous devez rejoindre cette activité avant de valider votre présence.';
          }
        },
        error: () => {
          this.loading = false;
          this.blocked = true;
          this.blockMessage = 'Impossible de vérifier votre éligibilité. Veuillez réessayer.';
        }
      });
  }

  startCheckIn(): void {
    this.loading = true;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.lastLatitude = pos.coords.latitude;
        this.lastLongitude = pos.coords.longitude;
        this.attendanceService.initiateCheckIn(this.activityId, this.lastLatitude, this.lastLongitude).subscribe({
          next: (res) => {
            this.loading = false;
            this.qrCode = res.qrCode;
            this.qrImageSrc = qrCodeImageSrc(res.qrCode);
            this.activityName = res.activityName;
            this.checkInDeadline = res.checkInDeadline;
          },
          error: (err) => {
            this.loading = false;
            this.snackBar.open(err.error?.error || 'Échec du démarrage du check-in', 'Fermer', { duration: 4000 });
          }
        });
      },
      () => {
        this.loading = false;
        this.snackBar.open('La géolocalisation est requise.', 'Fermer', { duration: 3000 });
      }
    );
  }

  confirmCheckIn(): void {
    this.loading = true;
    this.attendanceService.confirmCheckIn(
      this.activityId,
      'https://example.com/checkin-evidence.jpg',
      this.lastLatitude,
      this.lastLongitude
    ).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        this.snackBar.open('Présence enregistrée — en attente de confirmation par l\'hôte.', 'Fermer', { duration: 4000 });
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.error || 'Échec de la validation', 'Fermer', { duration: 5000 });
      }
    });
  }
}

