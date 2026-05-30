import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';

function passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss']
})
export class SignupComponent {
  signupForm: FormGroup;
  loading = false;
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.signupForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required]
    }, { validators: passwordMatchValidator });
  }

  get passwordMismatch(): boolean {
    return this.signupForm.hasError('passwordMismatch') &&
      (this.signupForm.get('confirmPassword')?.dirty ?? false);
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  getPasswordStrength(): string {
    const password = this.signupForm.get('password')?.value || '';
    
    if (password.length === 0) return '';
    if (password.length < 6) return 'weak';
    
    let score = 0;
    
    // Length check
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    
    // Character variety checks
    if (/[a-z]/.test(password)) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    
    if (score <= 2) return 'weak';
    if (score <= 4) return 'medium';
    if (score <= 5) return 'strong';
    return 'very-strong';
  }

  getPasswordStrengthText(): string {
    const strength = this.getPasswordStrength();
    switch (strength) {
      case 'weak': return 'Faible';
      case 'medium': return 'Moyen';
      case 'strong': return 'Fort';
      case 'very-strong': return 'Très fort';
      default: return '';
    }
  }

  onSubmit() {
    if (this.signupForm.invalid) return;

    this.loading = true;
    const { email, password, firstName, lastName } = this.signupForm.value;
    this.authService.signup({ email, password, firstName, lastName }).subscribe({
      next: () => {
        this.router.navigate(['/signup-success'], { queryParams: { email } });
      },
      error: (err: any) => {
        this.loading = false;
        this.snackBar.open(err.error?.error || 'Registration failed', 'Close', { duration: 3000 });
      }
    });
  }
}

