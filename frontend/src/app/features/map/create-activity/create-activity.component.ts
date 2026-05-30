import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivityService } from '../../../core/services/activity.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-create-activity',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: './create-activity.component.html',
  styleUrls: ['./create-activity.component.scss']
})
export class CreateActivityComponent {
  @Output() created = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();

  activityForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder, 
    private activityService: ActivityService,
    private snackBar: MatSnackBar
  ) {
    this.activityForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      activityType: ['MEETUP', Validators.required],
      participationMode: ['DIRECT', Validators.required],
      maxParticipants: [null],
      scheduledAt: ['', Validators.required],
      scheduledTime: ['18:00', Validators.required], // Heure par défaut à 18h00
      durationMinutes: [120],
      category: [''],
      
      // Coordinates
      latitude: [null],
      longitude: [null],
      startLatitude: [null],
      startLongitude: [null],
      destLatitude: [null],
      destLongitude: [null],
      address: ['']
    });
  }

  // Called when map clicks occur (we'll emit an event from MapComponent)
  setLocation(lat: number, lng: number) {
    if (this.activityForm.get('activityType')?.value === 'MEETUP') {
      this.activityForm.patchValue({ latitude: lat, longitude: lng });
    } else {
      // Logic for TRIP (start vs dest)
      if (!this.activityForm.get('startLatitude')?.value) {
        this.activityForm.patchValue({ startLatitude: lat, startLongitude: lng });
        this.snackBar.open('📍 Trip start point set!', 'OK', { duration: 2500 });
      } else {
        this.activityForm.patchValue({ destLatitude: lat, destLongitude: lng });
        this.snackBar.open('🏁 Trip destination set!', 'OK', { duration: 2500 });
      }
    }
  }

  onSubmit() {
    if (this.activityForm.invalid) return;

    this.loading = true;
    const formValue = this.activityForm.value;
    const payload = {
      ...formValue,
      scheduledAt: this.toLocalDateTime(formValue.scheduledAt, formValue.scheduledTime)
    };

    // Remove scheduledTime from payload as it's not needed by the backend
    delete payload.scheduledTime;

    this.activityService.create(payload).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.created.emit(res);
      },
      error: (err: any) => {
        this.loading = false;
        console.error('Failed to create activity', err);
        const errMsg = err?.error?.error || 'Failed to create activity. Please pick a future date and try again.';
        this.snackBar.open(errMsg, 'Close', { duration: 5000 });
      }
    });
  }

  private toLocalDateTime(dateValue: unknown, timeValue: unknown): string {
    if (!dateValue || !timeValue) {
      return '';
    }

    const dateStr = String(dateValue);
    const timeStr = String(timeValue);
    
    // Parse date (format: YYYY-MM-DD)
    const dateParts = dateStr.split('-');
    if (dateParts.length !== 3) {
      return '';
    }
    
    // Parse time (format: HH:MM)
    const timeParts = timeStr.split(':');
    if (timeParts.length !== 2) {
      return '';
    }
    
    const year = parseInt(dateParts[0], 10);
    const month = parseInt(dateParts[1], 10) - 1; // Month is 0-indexed in Date
    const day = parseInt(dateParts[2], 10);
    const hours = parseInt(timeParts[0], 10);
    const minutes = parseInt(timeParts[1], 10);
    
    const selectedDateTime = new Date(year, month, day, hours, minutes, 0, 0);
    
    // Ensure the selected date/time is in the future (with a small margin)
    const now = new Date();
    const minFuture = new Date(now.getTime() + 5 * 60 * 1000); // 5 minutes from now
    
    if (selectedDateTime <= minFuture) {
      // If selected time is too close to now, adjust it to be at least 5 minutes in the future
      const adjustedDateTime = new Date(minFuture.getTime() + 60 * 1000); // Add 1 more minute for safety
      const pad = (num: number) => num.toString().padStart(2, '0');
      return `${adjustedDateTime.getFullYear()}-${pad(adjustedDateTime.getMonth() + 1)}-${pad(adjustedDateTime.getDate())}T${pad(adjustedDateTime.getHours())}:${pad(adjustedDateTime.getMinutes())}:00`;
    }
    
    // Format as ISO string for backend (YYYY-MM-DDTHH:MM:SS)
    const pad = (num: number) => num.toString().padStart(2, '0');
    return `${year}-${pad(month + 1)}-${pad(day)}T${pad(hours)}:${pad(minutes)}:00`;
  }
}
