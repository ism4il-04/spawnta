import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.snackBar.open('Login successful!', 'Close', { duration: 3000 });
        this.router.navigate(['/profile']);
      },
      error: (err: any) => {
        this.loading = false;
        if (err.error?.code === 'EMAIL_NOT_VERIFIED') {
          const email: string = err.error.email ?? this.loginForm.value.email;
          this.snackBar.open('Please verify your email first.', 'Resend', { duration: 6000 })
            .onAction().subscribe(() => {
              this.authService.resendVerification(email).subscribe({
                next: () => this.snackBar.open('Verification email resent!', 'Close', { duration: 3000 }),
                error: () => this.snackBar.open('Could not resend email.', 'Close', { duration: 3000 })
              });
            });
        } else {
          this.snackBar.open(err.error?.error || 'Login failed', 'Close', { duration: 3000 });
        }
      }
    });
  }
}

