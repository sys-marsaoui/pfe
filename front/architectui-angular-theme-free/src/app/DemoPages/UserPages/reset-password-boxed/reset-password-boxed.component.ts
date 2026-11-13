import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth-service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-reset-password-boxed',
  templateUrl: './reset-password-boxed.component.html',
    styleUrls: ['reset-password.component.scss']  // Added: Load component-specific styles

})
export class ResetPasswordBoxedComponent implements OnInit {
  token: string | null = null;
  newPassword: string = '';
  confirmPassword: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.errorMessage = 'Invalid or missing reset token';
      }
    });
  }

  onSubmit(): void {
    if (!this.token) {
      this.errorMessage = 'Invalid reset link';
      return;
    }
    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Please fill in both password fields';
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }
    if (this.newPassword.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.resetPassword(this.token, this.newPassword).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'Password reset successfully! Redirecting to login...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to reset password. The token may be invalid or expired.';
      }
    });
  }
}