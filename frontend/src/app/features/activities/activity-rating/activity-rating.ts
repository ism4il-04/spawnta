import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AttendanceService, RatingEntry, ActivityStats } from '../../../core/services/attendance.service';

@Component({
  selector: 'app-activity-rating',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatSnackBarModule
  ],
  templateUrl: './activity-rating.html',
  styleUrl: './activity-rating.scss'
})
export class ActivityRatingComponent implements OnChanges {
  @Input() activityId!: number;
  @Input() isHost = false;
  @Input() canRate = false;
  @Input() hasRated = false;

  private attendanceService = inject(AttendanceService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  stats: ActivityStats | null = null;
  ratings: RatingEntry[] = [];
  ratingForm!: FormGroup;
  hoverScore = 0;
  submitting = false;
  loadError = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activityId']?.currentValue) {
      this.ratingForm = this.fb.group({
        ratingScore: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
        comment: ['', Validators.maxLength(500)]
      });
      this.loadData();
    }
  }

  loadData(): void {
    if (!this.activityId) return;
    this.loadError = false;
    this.attendanceService.getActivityStats(this.activityId).subscribe({
      next: (s) => this.stats = s,
      error: () => this.loadError = true
    });
    this.attendanceService.getActivityRatings(this.activityId).subscribe({
      next: (r) => this.ratings = r,
      error: () => this.loadError = true
    });
  }

  setHover(score: number): void {
    if (!this.submitting) this.hoverScore = score;
  }

  setRating(score: number): void {
    this.ratingForm.patchValue({ ratingScore: score });
  }

  get currentScore(): number {
    return this.ratingForm.get('ratingScore')?.value || 0;
  }

  submitRating(): void {
    if (this.ratingForm.invalid) {
      this.snackBar.open('Veuillez sélectionner une note de 1 à 5', 'Fermer', { duration: 3000 });
      return;
    }

    this.submitting = true;
    const { ratingScore, comment } = this.ratingForm.value;

    this.attendanceService.rateActivity(this.activityId, ratingScore, comment).subscribe({
      next: () => {
        this.submitting = false;
        this.hasRated = true;
        this.snackBar.open('Évaluation enregistrée !', 'Fermer', { duration: 3000 });
        this.ratingForm.reset({ ratingScore: 0, comment: '' });
        this.loadData();
      },
      error: (err) => {
        this.submitting = false;
        this.snackBar.open(err.error?.error || 'Erreur lors de l\'enregistrement', 'Fermer', { duration: 4000 });
      }
    });
  }
}
