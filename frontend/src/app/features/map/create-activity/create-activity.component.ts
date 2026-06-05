import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivityResponse, ActivityService } from '../../../core/services/activity.service';
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
export class CreateActivityComponent implements OnChanges {
  @Input() editActivity: ActivityResponse | null = null;
  @Output() created = new EventEmitter<any>();
  @Output() updated = new EventEmitter<any>();
  @Output() cancel = new EventEmitter<void>();
  @Output() hide = new EventEmitter<void>(); // Nouveau: pour cacher le formulaire

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
      scheduledTime: ['18:00', Validators.required],
      durationDays: [0],
      durationHours: [2],
      durationMins: [0],
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

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['editActivity'] && this.editActivity) {
      this.fillFormForEdit(this.editActivity);
    }
  }

  private fillFormForEdit(activity: ActivityResponse): void {
    const scheduled = new Date(activity.scheduledAt);
    const durationTotal = activity.durationMinutes || 0;
    const days = Math.floor(durationTotal / (24 * 60));
    const hours = Math.floor((durationTotal % (24 * 60)) / 60);
    const mins = durationTotal % 60;

    const pad = (num: number) => num.toString().padStart(2, '0');
    const timeStr = `${pad(scheduled.getHours())}:${pad(scheduled.getMinutes())}`;

    this.activityForm.patchValue({
      title: activity.title,
      description: activity.description,
      activityType: activity.activityType,
      participationMode: activity.participationMode,
      maxParticipants: activity.maxParticipants,
      scheduledAt: scheduled,
      scheduledTime: timeStr,
      durationDays: days,
      durationHours: hours,
      durationMins: mins,
      category: activity.category,
      latitude: activity.latitude,
      longitude: activity.longitude,
      startLatitude: activity.startLatitude,
      startLongitude: activity.startLongitude,
      destLatitude: activity.destLatitude,
      destLongitude: activity.destLongitude,
      address: activity.address
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
        this.snackBar.open('Trip start point set!', 'OK', { duration: 2500 });
      } else {
        this.activityForm.patchValue({ destLatitude: lat, destLongitude: lng });
        this.snackBar.open('Trip destination set!', 'OK', { duration: 2500 });
      }
    }
  }

  // Permet de cliquer en dehors du formulaire sans le fermer
  onOverlayClick(event: MouseEvent) {
    // Ne rien faire - les clics passent à travers vers la carte
    // Le formulaire reste ouvert pour voir les coordonnées sélectionnées
  }

  // Cache le formulaire temporairement pour sélectionner sur la carte
  hideForm() {
    this.hide.emit();
  }

  onSubmit() {
    if (this.activityForm.invalid) return;

    this.loading = true;
    const formValue = this.activityForm.value;

    // Compute total duration in minutes from days/hours/minutes
    const totalMinutes =
      (formValue.durationDays || 0) * 24 * 60 +
      (formValue.durationHours || 0) * 60 +
      (formValue.durationMins || 0);

    const payload = {
      ...formValue,
      scheduledAt: this.toLocalDateTime(formValue.scheduledAt, formValue.scheduledTime),
      durationMinutes: totalMinutes || null
    };

    // Remove helper fields not needed by the backend
    delete payload.scheduledTime;
    delete payload.durationDays;
    delete payload.durationHours;
    delete payload.durationMins;

    if (this.editActivity) {
      this.activityService.updateActivity(this.editActivity.id, payload).subscribe({
        next: (res: any) => {
          this.loading = false;
          this.updated.emit(res);
          this.snackBar.open('✅ Activity updated!', 'OK', { duration: 3000 });
        },
        error: (err: any) => {
          this.loading = false;
          console.error('Failed to update activity', err);
          const errMsg = err?.error?.error || 'Failed to update activity.';
          this.snackBar.open(errMsg, 'Close', { duration: 5000 });
        }
      });
    } else {
      this.activityService.createActivity(payload).subscribe({
        next: (res: any) => {
          this.loading = false;
          this.created.emit(res);
          this.snackBar.open('🎉 Activity created!', 'OK', { duration: 3000 });
        },
        error: (err: any) => {
          this.loading = false;
          console.error('Failed to create activity', err);
          const errMsg = err?.error?.error || 'Failed to create activity. Please pick a future date and try again.';
          this.snackBar.open(errMsg, 'Close', { duration: 5000 });
        }
      });
    }
  }

  private toLocalDateTime(dateValue: unknown, timeValue: unknown): string {
    if (!dateValue || !timeValue) {
      return '';
    }

    let year: number;
    let month: number;
    let day: number;

    if (dateValue instanceof Date) {
      year = dateValue.getFullYear();
      month = dateValue.getMonth();
      day = dateValue.getDate();
    } else {
      const dateStr = String(dateValue);
      const dateParts = dateStr.split('-');
      if (dateParts.length === 3) {
        year = parseInt(dateParts[0], 10);
        month = parseInt(dateParts[1], 10) - 1;
        day = parseInt(dateParts[2], 10);
      } else {
        const parsed = new Date(dateStr);
        if (isNaN(parsed.getTime())) {
          return '';
        }
        year = parsed.getFullYear();
        month = parsed.getMonth();
        day = parsed.getDate();
      }
    }

    const timeStr = String(timeValue);
    const timeParts = timeStr.split(':');
    if (timeParts.length !== 2) {
      return '';
    }

    const hours = parseInt(timeParts[0], 10);
    const minutes = parseInt(timeParts[1], 10);

    const selectedDateTime = new Date(year, month, day, hours, minutes, 0, 0);
    const now = new Date();
    const minFuture = new Date(now.getTime() + 5 * 60 * 1000); // 5 minutes from now

    if (selectedDateTime <= minFuture) {
      const adjustedDateTime = new Date(minFuture.getTime() + 60 * 1000);
      const pad = (num: number) => num.toString().padStart(2, '0');
      return `${adjustedDateTime.getFullYear()}-${pad(adjustedDateTime.getMonth() + 1)}-${pad(adjustedDateTime.getDate())}T${pad(adjustedDateTime.getHours())}:${pad(adjustedDateTime.getMinutes())}:00`;
    }

    const pad = (num: number) => num.toString().padStart(2, '0');
    return `${year}-${pad(month + 1)}-${pad(day)}T${pad(hours)}:${pad(minutes)}:00`;
  }
}
