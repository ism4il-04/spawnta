import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, finalize } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ProfileService, UserProfile } from '../../core/services/profile.service';
import { AuthService } from '../../core/services/auth.service';
import { GamificationService, GamificationProfile, Badge, LeaderboardEntry } from '../../core/services/gamification.service';

export const ALL_INTERESTS = [
  'HIKING', 'CYCLING', 'RUNNING', 'SWIMMING', 'YOGA',
  'PHOTOGRAPHY', 'COOKING', 'MUSIC', 'GAMING', 'TRAVEL',
  'READING', 'CINEMA', 'ART', 'DANCE', 'FITNESS',
  'CLIMBING', 'SKIING', 'SURFING', 'BASKETBALL', 'FOOTBALL',
  'TENNIS', 'VOLUNTEERING', 'LANGUAGES', 'TECH', 'NATURE'
];

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  profileLoadError = false;
  loadingProfile = true;
  profileForm!: FormGroup;
  selectedInterests: Set<string> = new Set();
  allInterests = ALL_INTERESTS;
  loading = false;
  savingInterests = false;
  isOwnProfile = true;

  // Gamification Fields
  gProfile: GamificationProfile | null = null;
  allBadges: Badge[] = [];
  leaderboard: LeaderboardEntry[] = [];
  gamificationLoading = false;
  gamificationError = false;

  constructor(
    private fb: FormBuilder,
    private profileService: ProfileService,
    private authService: AuthService,
    private gamificationService: GamificationService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.profileLoadError = false;
    this.profileForm = this.fb.group({
      bio: ['', Validators.maxLength(500)],
      facebook: ['', Validators.maxLength(255)],
      instagram: ['', Validators.maxLength(255)],
      whatsapp: ['', Validators.maxLength(50)],
      profilePublic: [true]
    });

    this.route.paramMap.subscribe(params => {
      const userId = Number(params.get('id'));
      this.isOwnProfile = !userId;
      this.loadProfile(userId || null);
    });
  }

  private loadProfile(userId: number | null): void {
    this.loadingProfile = true;
    this.profileLoadError = false;
    const profile$ = userId
      ? this.profileService.getProfileById(userId)
      : this.profileService.getProfile();

    profile$.subscribe({
      next: (p: UserProfile) => {
        this.loadingProfile = false;
        this.profile = p;
        this.selectedInterests = new Set(p.interests);
        if (this.isOwnProfile) {
          this.profileForm.patchValue({
            bio: p.bio ?? '',
            facebook: p.facebook ?? '',
            instagram: p.instagram ?? '',
            whatsapp: p.whatsapp ?? '',
            profilePublic: p.profilePublic
          });
          this.loadGamificationData();
        } else {
          this.gProfile = null;
          this.allBadges = [];
          this.leaderboard = [];
        }
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.loadingProfile = false;
        this.cdr.detectChanges();
        if (err.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
        } else {
          this.profileLoadError = true;
          this.snackBar.open('Failed to load profile', 'Close', { duration: 3000 });
        }
      }
    });
  }

  loadGamificationData(): void {
    this.gamificationLoading = true;
    this.gamificationError = false;

    forkJoin({
      profile: this.gamificationService.getGamificationProfile(),
      badges: this.gamificationService.getBadges(),
      leaderboard: this.gamificationService.getLeaderboard()
    }).pipe(
      finalize(() => {
        this.gamificationLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: ({ profile, badges, leaderboard }) => {
        this.gProfile = profile;
        this.allBadges = badges;
        this.leaderboard = leaderboard;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.gamificationError = true;
        console.error('Gamification API error', err);
        if (err.status === 401 || err.status === 403) {
          this.authService.logout();
          this.router.navigate(['/login']);
          return;
        }
        this.snackBar.open(
          'Could not load XP, badges or leaderboard. Restart the backend and try again.',
          'Close',
          { duration: 5000 }
        );
        this.cdr.detectChanges();
      }
    });
  }

  hasBadge(badgeId: number): boolean {
    if (!this.gProfile) return false;
    return this.gProfile.achievements.some(a => a.id === badgeId);
  }

  toggleInterest(interest: string): void {
    if (this.selectedInterests.has(interest)) {
      this.selectedInterests.delete(interest);
    } else {
      if (this.selectedInterests.size >= 10) {
        this.snackBar.open('Maximum 10 interests allowed', 'Close', { duration: 2000 });
        return;
      }
      this.selectedInterests.add(interest);
    }
  }

  isSelected(interest: string): boolean {
    return this.selectedInterests.has(interest);
  }

  onSaveProfile(): void {
    if (this.profileForm.invalid) return;
    this.loading = true;
    const formVal = this.profileForm.value;
    this.profileService.updateProfile({
      bio: formVal.bio || null,
      facebook: formVal.facebook || null,
      instagram: formVal.instagram || null,
      whatsapp: formVal.whatsapp || null,
      visitedCountries: this.profile?.visitedCountries ?? [],
      profilePublic: formVal.profilePublic
    }).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (p: UserProfile) => {
        this.profile = p;
        this.snackBar.open('Profile updated!', 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Failed to update profile', 'Close', { duration: 3000 });
      }
    });
  }

  onSaveInterests(): void {
    this.savingInterests = true;
    this.profileService.updateInterests([...this.selectedInterests]).subscribe({
      next: (p: UserProfile) => {
        this.profile = p;
        this.savingInterests = false;
        this.snackBar.open('Interests saved!', 'Close', { duration: 3000 });
      },
      error: () => {
        this.savingInterests = false;
        this.snackBar.open('Failed to save interests', 'Close', { duration: 3000 });
      }
    });
  }

  onAvatarChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.profileService.uploadAvatar(file).subscribe({
      next: (p: UserProfile) => {
        this.profile = p;
        this.snackBar.open('Avatar updated!', 'Close', { duration: 3000 });
      },
      error: () => this.snackBar.open('Avatar upload failed', 'Close', { duration: 3000 })
    });
  }

  onGalleryAdd(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.profileService.addGalleryPhoto(file).subscribe({
      next: (p: UserProfile) => {
        this.profile = p;
        this.snackBar.open('Photo added to gallery!', 'Close', { duration: 3000 });
      },
      error: () => this.snackBar.open('Gallery upload failed', 'Close', { duration: 3000 })
    });
  }

  onGalleryRemove(url: string): void {
    this.profileService.removeGalleryPhoto(url).subscribe({
      next: (p: UserProfile) => {
        this.profile = p;
        this.snackBar.open('Photo removed', 'Close', { duration: 2000 });
      },
      error: () => this.snackBar.open('Failed to remove photo', 'Close', { duration: 3000 })
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
