import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/core/services/auth-service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password-boxed',
  templateUrl: './forgot-password-boxed.component.html',
  styleUrls: ['forgot-password-boxed.component.scss']  // Added: Load component-specific styles
})
export class ForgotPasswordBoxedComponent implements OnInit {
  email: string = '';

  constructor(
    private authService: AuthService,
    private toastrService: ToastrService,
    private router: Router
  ) {}

  ngOnInit() {}

  recoverPassword() {
    this.authService.forgotPassword(this.email).subscribe(
      (response) => {
        if (response && response.code === '200') {
          this.toastrService.success('Password recovery email sent');
          this.router.navigateByUrl('/login');
        } else {
          this.toastrService.error('Failed to send recovery email');
        }
      },
      (error) => {
        this.toastrService.error('Error: ' + error.message);
      }
    );
  }
}