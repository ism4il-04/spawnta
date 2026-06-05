import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AttendanceService, ParticipationStatus } from '../../../core/services/attendance.service';
import { Html5Qrcode } from 'html5-qrcode';
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
export class CheckInComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private attendanceService = inject(AttendanceService);
  private snackBar = inject(MatSnackBar);

  activityId!: number;
  loading = false;
  loadingMessage = 'Checking eligibility...';
  success = false;
  blocked = false;
  blockMessage = '';
  activityName = '';
  attendanceStatus: string | null = null;

  activeMethod: 'QR' | 'GPS' = 'QR';
  isScannerActive = false;
  private html5Qrcode?: Html5Qrcode;

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

  ngOnDestroy(): void {
    this.stopScanner();
  }

  private verifyEligibility(): void {
    this.loading = true;
    this.loadingMessage = 'Checking eligibility...';
    this.attendanceService.getMyParticipationStatus(this.activityId)
      .pipe(timeout(10000))
      .subscribe({
        next: (status: ParticipationStatus) => {
          this.loading = false;
          this.attendanceStatus = status.attendanceStatus;

          if (status.host) {
            this.blocked = true;
            this.blockMessage = 'Hosts do not need to check in. Please show your activity QR code from the map details panel for participants to scan.';
            return;
          }

          if (status.attendanceStatus === 'CONFIRMED') {
            this.blocked = true;
            return;
          }

          if (!status.canCheckIn) {
            this.blocked = true;
            this.blockMessage = status.joined
              ? 'Check-in is currently unavailable. You must check in within the activity time window.'
              : 'You must join this activity before validating your presence.';
          }
        },
        error: () => {
          this.loading = false;
          this.blocked = true;
          this.blockMessage = 'Unable to verify eligibility. Please try again.';
        }
      });
  }

  setMethod(method: 'QR' | 'GPS'): void {
    this.activeMethod = method;
    if (method !== 'QR') {
      this.stopScanner();
    }
  }

  startScanner(): void {
    this.isScannerActive = true;
    setTimeout(() => {
      try {
        this.html5Qrcode = new Html5Qrcode('reader');
        this.html5Qrcode.start(
          { facingMode: 'environment' },
          {
            fps: 10,
            qrbox: { width: 250, height: 250 }
          },
          (decodedText) => {
            this.stopScanner();
            this.onQrTokenScanned(decodedText);
          },
          () => { }
        ).catch(err => {
          console.warn('Environment camera failed, trying user camera...', err);
          return this.html5Qrcode!.start(
            { facingMode: 'user' },
            {
              fps: 10,
              qrbox: { width: 250, height: 250 }
            },
            (decodedText) => {
              this.stopScanner();
              this.onQrTokenScanned(decodedText);
            },
            () => { }
          );
        }).catch(err => {
          console.error('Camera startup error', err);
          this.isScannerActive = false;
          this.snackBar.open('Unable to access camera. Please allow permission.', 'Close', { duration: 4000 });
        });
      } catch (e) {
        console.error(e);
        this.isScannerActive = false;
      }
    }, 100);
  }

  stopScanner(): void {
    this.isScannerActive = false;
    if (this.html5Qrcode) {
      if (this.html5Qrcode.isScanning) {
        this.html5Qrcode.stop().catch(err => console.error('Error stopping scanner', err));
      }
      this.html5Qrcode = undefined;
    }
  }

  private onQrTokenScanned(token: string): void {
    this.loading = true;
    this.loadingMessage = 'Validating QR token...';
    this.attendanceService.checkInViaQr(this.activityId, token).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        this.snackBar.open('🎉 Attendance confirmed successfully via QR!', 'Close', { duration: 4000 });
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.error || 'QR Validation failed. Invalid or expired token.', 'Close', { duration: 5000 });
      }
    });
  }

  confirmCheckInGps(): void {
    this.loading = true;
    this.loadingMessage = 'Retrieving GPS position...';
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.loadingMessage = 'Validating location coordinates...';
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        this.attendanceService.confirmCheckIn(this.activityId, lat, lng).subscribe({
          next: () => {
            this.loading = false;
            this.success = true;
            this.snackBar.open('🎉 Geolocation check-in successful!', 'Close', { duration: 4000 });
          },
          error: (err) => {
            this.loading = false;
            this.snackBar.open(err.error?.error || 'Validation failed. Ensure you are close to the location.', 'Close', { duration: 5000 });
          }
        });
      },
      () => {
        this.loading = false;
        this.snackBar.open('Geolocation permission is required for GPS check-in.', 'Close', { duration: 4000 });
      }
    );
  }
}
