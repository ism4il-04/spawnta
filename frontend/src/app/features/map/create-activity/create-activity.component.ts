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
  styleUrl: './create-activity.component.scss'
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
    const payload = {
      ...this.activityForm.value,
      scheduledAt: this.toLocalDateTime(this.activityForm.value.scheduledAt)
    };

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

  private toLocalDateTime(value: unknown): string {
    if (value instanceof Date) {
      const baseDate = new Date(
        value.getFullYear(),
        value.getMonth(),
        value.getDate(),
        18,
        0,
        0,
        0
      );
      const now = new Date();
      const minFuture = new Date(now.getTime() + 60 * 60 * 1000);
      const sameDay =
        baseDate.getFullYear() === now.getFullYear() &&
        baseDate.getMonth() === now.getMonth() &&
        baseDate.getDate() === now.getDate();
      const localDate = sameDay && baseDate <= minFuture ? minFuture : baseDate;
      const pad = (part: number) => part.toString().padStart(2, '0');
      return `${localDate.getFullYear()}-${pad(localDate.getMonth() + 1)}-${pad(localDate.getDate())}T${pad(localDate.getHours())}:${pad(localDate.getMinutes())}`;
    }

    return String(value ?? '');
  }
}
